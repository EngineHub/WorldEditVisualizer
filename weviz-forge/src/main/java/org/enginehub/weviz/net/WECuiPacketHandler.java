/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.weviz.net;

import static org.enginehub.weviz.net.PacketHandlerUtil.sendToServer;


import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkInstance;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.enginehub.weviz.state.Shape;
import org.enginehub.weviz.state.VizState;

public final class WECuiPacketHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int PROTOCOL_VERSION = 1;
    private static final ResourceLocation CUI_CHANNEL = new ResourceLocation("worldedit", "cui");
    private static final String CUI_VERSION = "3";

    private interface DataHandler {

        static DataHandler from(int length, BiFunction<VizState, double[], Either<VizState, String>> handler) {
            return new DataHandler() {
                @Override
                public int getLength() {
                    return length;
                }

                @Override
                public Either<VizState, String> handle(VizState state, double[] data) {
                    return handler.apply(state, data);
                }
            };
        }

        int getLength();

        Either<VizState, String> handle(VizState state, double[] data);

    }

    private static final Map<String, DataHandler> DATA_HANDLERS = ImmutableMap.<String, DataHandler>builder()
        .put("p", DataHandler.from(5, (state, data) ->
            Either.left(state.toBuilder()
                .addPoint((int) data[0], Vector3.at(data[1], data[2], data[3]))
                .build())
        ))
        .put("e", DataHandler.from(4, (state, data) -> {
            Vector3 vec = Vector3.at(data[1], data[2], data[3]);
            switch ((int) data[0]) {
                case 0:
                    return Either.left(state.toBuilder().ellipsoidCenter(vec).build());
                case 1:
                    return Either.left(state.toBuilder().ellipsoidRadii(vec).build());
                default:
                    return Either.right("unknown ellipsoid id");
            }
        }))
        .put("p2", DataHandler.from(4, (state, data) ->
            Either.left(state.toBuilder()
                .addPoint2d((int) data[0], Vector2.at(data[1], data[2]))
                .build())
        ))
        .put("cyl", DataHandler.from(5, (state, data) ->
            Either.left(state.toBuilder()
                .cylinderCenter(Vector3.at(data[0], data[1], data[2]))
                .cylinderRadii(Vector2.at(data[3], data[4]))
                .build())
        ))
        .put("mm", DataHandler.from(2, (state, data) -> {
            if (data[0] > data[1]) {
                return Either.right("min > max");
            }
            return Either.left(state.toBuilder().min(data[0]).max(data[1]).build());
        }))
        // technically this could be something besides 3, but we don't know how to render it
        .put("poly", DataHandler.from(3, (state, data) ->
            Either.left(state.toBuilder().addPolygon(new DoubleArrayList(data)).build())
        ))
        .build();

    private final AtomicReference<VizState> state;
    private volatile boolean seenMalformed;

    public WECuiPacketHandler(AtomicReference<VizState> state) {
        this.state = state;
        // we don't need to keep a strong reference to it, forge does
//        EventNetworkChannel handler = PacketHandlerUtil
//            .buildLenientHandler(CUI_CHANNEL, PROTOCOL_VERSION);
        // temporary hack until forge gets gud
        EventNetworkChannel handler;
        try {
            Method findTarget = NetworkRegistry.class.getDeclaredMethod("findTarget", ResourceLocation.class);
            findTarget.setAccessible(true);
            @SuppressWarnings("unchecked")
            Optional<NetworkInstance> networkInstance =
                (Optional<NetworkInstance>) findTarget.invoke(null, CUI_CHANNEL);
            handler = new EventNetworkChannel(
                networkInstance.orElseThrow(() -> new IllegalStateException("no registered CUI channel?"))
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        handler.addListener(this::onPacketData);
        handler.addListener(this::onChannelRegistrationChange);
    }

    private void warnOnMalformed(String reason, String text) {
        if (!seenMalformed) {
            LOGGER.warn("Malformed CUI packet received (" + reason + "): " + text);
            seenMalformed = true;
        }
    }

    private OptionalDouble tryParseDouble(String text) {
        try {
            return OptionalDouble.of(Double.parseDouble(text));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    private void onPacketData(NetworkEvent.ServerCustomPayloadEvent event) {
        event.getSource().get().setPacketHandled(true);
        String text = event.getPayload().toString(StandardCharsets.UTF_8);
        String[] payload = text.split("\\|");
        if (payload.length == 1) {
            warnOnMalformed("no '|'", text);
            return;
        }
        String type = payload[0].toLowerCase(Locale.ROOT);

        // special case -- all other potential types use ints everywhere
        if ("s".equals(type)) {
            if (payload.length != 2) {
                warnOnMalformed("no shape arg", text);
                return;
            }
            Shape shape = Shape.getById(payload[1]);
            if (shape == null) {
                warnOnMalformed("unknown shape", text);
                return;
            }
            VizState newState = VizState.builder()
                .shape(shape)
                .build();
            state.set(newState);
            System.err.println("New state set: " + newState);
            return;
        }

        double[] data = Arrays.stream(payload).skip(1).map(this::tryParseDouble)
            .filter(OptionalDouble::isPresent)
            .mapToDouble(OptionalDouble::getAsDouble)
            .toArray();
        if (data.length < payload.length - 1) {
            // some of the fields didn't parse as ints
            warnOnMalformed("not all int args", text);
            return;
        }
        DataHandler handler = DATA_HANDLERS.get(type);
        if (handler == null) {
            warnOnMalformed("no handler for type", text);
            return;
        } else if (data.length != handler.getLength()) {
            warnOnMalformed("wrong length for type", text);
            return;
        }
        while (true) {
            VizState oldState = state.get();
            if (oldState == null) {
                // this should never happen! we need shape information first.
                warnOnMalformed("no old state", text);
                return;
            }
            Either<VizState, String> newState = handler.handle(oldState, data);
            if (newState.right().isPresent()) {
                warnOnMalformed(newState.right().get(), text);
                return;
            }
            assert newState.left().isPresent();
            // realistically this should never contend, but we'll be thread-safe anyways :)
            if (state.compareAndSet(oldState, newState.left().get())) {
                System.err.println("New state set: " + newState.left().get());
                return;
            }
        }
    }

    private void onChannelRegistrationChange(NetworkEvent.ChannelRegistrationChangeEvent event) {
        if (event.getRegistrationChangeType() == NetworkEvent.RegistrationChangeType.REGISTER) {
            // we no longer have seen a malformed packet on this connection
            seenMalformed = false;
            // clear state
            state.set(null);
            // Send the handshake
            sendToServer(CUI_CHANNEL, "v|" + CUI_VERSION);
        }
    }

}
