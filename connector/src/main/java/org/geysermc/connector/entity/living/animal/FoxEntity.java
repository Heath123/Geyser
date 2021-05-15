/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.connector.entity.living.animal;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import org.geysermc.connector.entity.type.EntityType;
import org.geysermc.connector.network.session.RorySession;
import org.geysermc.connector.network.translators.item.ItemEntry;

public class FoxEntity extends AnimalEntity {

    public FoxEntity(long entityId, long geyserId, EntityType entityType, Vector3f position, Vector3f motion, Vector3f rotation) {
        super(entityId, geyserId, entityType, position, motion, rotation);
    }

    @Override
    public void updateBedrockMetadata(EntityMetadata entityMetadata, RorySession session) {
        if (entityMetadata.getId() == 16) {
            metadata.put(EntityData.VARIANT, entityMetadata.getValue());
        }
        if (entityMetadata.getId() == 17) {
            byte xd = (byte) entityMetadata.getValue();
            metadata.getFlags().setFlag(EntityFlag.SITTING, (xd & 0x01) == 0x01);
            metadata.getFlags().setFlag(EntityFlag.SNEAKING, (xd & 0x04) == 0x04);
            metadata.getFlags().setFlag(EntityFlag.INTERESTED, (xd & 0x08) == 0x08);
            metadata.getFlags().setFlag(EntityFlag.SLEEPING, (xd & 0x20) == 0x20);
        }
        super.updateBedrockMetadata(entityMetadata, session);
    }

    @Override
    public boolean canEat(RorySession session, String javaIdentifierStripped, ItemEntry itemEntry) {
        return javaIdentifierStripped.equals("sweet_berries");
    }
}
