/*
 * Copyright (c) 2019-2021 RoryMC. http://geysermc.org
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
 * @author RoryMC
 * @link https://github.com/RoryMC/Rory
 */

package org.geysermc.connector.network.session.cache;

import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.connector.network.session.RorySession;
import org.geysermc.connector.scoreboard.Objective;
import org.geysermc.connector.scoreboard.Scoreboard;
import org.geysermc.connector.scoreboard.ScoreboardUpdater;

@Getter
public class WorldCache {
    private final RorySession session;
    @Setter
    private Difficulty difficulty = Difficulty.EASY;

    private Scoreboard scoreboard;
    private final ScoreboardUpdater scoreboardUpdater;

    public WorldCache(RorySession session) {
        this.session = session;
        this.scoreboard = new Scoreboard(session);
        scoreboardUpdater = new ScoreboardUpdater(this);
        scoreboardUpdater.start();
    }

    public void removeScoreboard() {
        if (scoreboard != null) {
            for (Objective objective : scoreboard.getObjectives().values()) {
                scoreboard.despawnObjective(objective);
            }
            scoreboard = new Scoreboard(session);
        }
    }

    public int increaseAndGetScoreboardPacketsPerSecond() {
        int pendingPps = scoreboardUpdater.incrementAndGetPacketsPerSecond();
        int pps = scoreboardUpdater.getPacketsPerSecond();
        return Math.max(pps, pendingPps);
    }
}