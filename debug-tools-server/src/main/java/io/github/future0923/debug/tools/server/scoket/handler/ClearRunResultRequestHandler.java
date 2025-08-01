/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.server.scoket.handler;

import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;
import io.github.future0923.debug.tools.common.handler.BasePacketHandler;
import io.github.future0923.debug.tools.common.protocal.packet.request.ClearRunResultRequestPacket;
import io.github.future0923.debug.tools.server.utils.DebugToolsResultUtils;

import java.io.OutputStream;

/**
 * @author future0923
 */
public class ClearRunResultRequestHandler extends BasePacketHandler<ClearRunResultRequestPacket> {

    public static final ClearRunResultRequestHandler INSTANCE = new ClearRunResultRequestHandler();

    private ClearRunResultRequestHandler() {

    }

    @Override
    public void handle(OutputStream outputStream, ClearRunResultRequestPacket packet) throws Exception {
        if (StrUtil.isNotBlank(packet.getFieldOffset())) {
            DebugToolsResultUtils.removeCache(packet.getFieldOffset());
        }
        if (StrUtil.isNotBlank(packet.getTraceOffset())) {
            DebugToolsResultUtils.removeCache(packet.getTraceOffset());
        }
    }
}
