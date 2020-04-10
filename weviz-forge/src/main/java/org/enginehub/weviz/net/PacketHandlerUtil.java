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

import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

final class PacketHandlerUtil {
    private PacketHandlerUtil() {
    }

    static EventNetworkChannel buildLenientHandler(ResourceLocation id, int protocolVersion) {
        final String verStr = Integer.toString(protocolVersion);
        final Predicate<String> validator = validateLenient(verStr);
        return NetworkRegistry.ChannelBuilder
            .named(id)
            .clientAcceptedVersions(validator)
            .serverAcceptedVersions(validator)
            .networkProtocolVersion(() -> verStr)
            .eventNetworkChannel();
    }

    private static Predicate<String> validateLenient(String protocolVersion) {
        return remoteVersion ->
            protocolVersion.equals(remoteVersion)
                || NetworkRegistry.ABSENT.equals(remoteVersion)
                || NetworkRegistry.ACCEPTVANILLA.equals(remoteVersion);
    }

    static void sendToServer(ResourceLocation channel, String packet) {
        ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            // delay for later
            Minecraft.getInstance().deferTask(() -> sendToServer(channel, packet));
            return;
        }
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeBytes(packet.getBytes(StandardCharsets.UTF_8));
        connection.getNetworkManager().sendPacket(
            new CCustomPayloadPacket(channel, buffer)
        );
    }
}
