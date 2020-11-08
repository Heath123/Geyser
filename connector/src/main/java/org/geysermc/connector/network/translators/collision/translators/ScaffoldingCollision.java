/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
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

package org.geysermc.connector.network.translators.collision.translators;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3d;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;
import com.nukkitx.protocol.bedrock.packet.SetEntityDataPacket;
import lombok.EqualsAndHashCode;
import org.geysermc.connector.entity.player.PlayerEntity;
import org.geysermc.connector.entity.type.EntityType;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.collision.BoundingBox;
import org.geysermc.connector.network.translators.collision.CollisionRemapper;

@CollisionRemapper(regex = "^scaffolding$", usesParams = true, passDefaultBoxes = true)
public class ScaffoldingCollision extends BlockCollision {
    /**
     * Some parts of some scaffolding blocks can be walked on in Java but not Bedrock
     * If the block contains a part that can only be walked on on Java, this will be true
     * new BoundingBox(0.5, 0.0625, 0.5, 1, 0.125, 1)
     */
    @EqualsAndHashCode.Include
    private boolean javaWalkable = false;

    /**
     * Stores the bounding box of the part that's only walkable on Java so movements can be cancelled
     */
    private static final BoundingBox javaWalkableBox = new BoundingBox(
            0.5, 0.0625, 0.5, 1, 0.125, 1
    );


    public ScaffoldingCollision(String params, BoundingBox[] defaultBoxes) {
        super();
        boundingBoxes = defaultBoxes;
        if (params.contains("bottom=true")) {
            javaWalkable = true;
        }
        // thing = javaWalkable;
    }

    @Override
    public boolean correctPosition(BoundingBox playerCollision, GeyserSession session) {
        if (this.checkIntersection(playerCollision)) {
            session.getCollisionManager().setTouchingScaffolding(true);
            session.getCollisionManager().setOnScaffolding(true);
        } else {
            // Hack to check slightly below the player
            playerCollision.setSizeY(playerCollision.getSizeY() + 0.001);
            playerCollision.setMiddleY(playerCollision.getMiddleY() - 0.002);

            if (this.checkIntersection(playerCollision)) {
                session.getCollisionManager().setOnScaffolding(true);
            }

            playerCollision.setSizeY(playerCollision.getSizeY() - 0.001);
            playerCollision.setMiddleY(playerCollision.getMiddleY() + 0.002);
        }

        if (javaWalkable) {
            if (javaWalkableBox.checkIntersection(x, y, z, playerCollision)) {
                // The player is allowed to move up into the scaffolding
                if (((playerCollision.getMiddleY() - (playerCollision.getSizeY() / 2)) -
                        (session.getPlayerEntity().getPosition().getY() - EntityType.PLAYER.getOffset())) > 0) {
                    return true;
                }
                System.out.println("disallow");

                    super.correctPosition(playerCollision, session);

                    // double newY = Math.floor(session.getPlayerEntity().getPosition().getY() - EntityType.PLAYER.getOffset()) + 0.125;

                    PlayerEntity entity = session.getPlayerEntity();

                    MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
                    movePlayerPacket.setRuntimeEntityId(entity.getGeyserId());
                    Vector3f bedrockPosition = Vector3f.from(
                            playerCollision.getMiddleX(), playerCollision.getMiddleY() + EntityType.PLAYER.getOffset(), playerCollision.getMiddleZ()
                    );
                    entity.setPosition(bedrockPosition, false);
                    movePlayerPacket.setPosition(bedrockPosition);
                    movePlayerPacket.setRotation(entity.getBedrockRotation());
                    movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
                    session.sendUpstreamPacket(movePlayerPacket);

                    Packet movePacket = new ClientPlayerPositionPacket(true, playerCollision.getMiddleX(), playerCollision.getMiddleY() - (session.getCollisionManager().getPlayerBoundingBox().getSizeY() / 2), playerCollision.getMiddleZ());
                    session.sendDownstreamPacket(movePacket);

                // If the box can only be walked on in Java Edition, the best we can do is cancel the movement.
                // return false;
            }
        }

        return true;
    }

    // Makes sure the optimisations in CollisionManager don't delete distinct instances from the cache as duplicates
    @Override
    public int hashCode() {
        return super.hashCode() + (javaWalkable ? 0 : 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ScaffoldingCollision) {
            return super.equals(o) && ((ScaffoldingCollision) o).javaWalkable == javaWalkable;
        }
        return false;
    }
}
