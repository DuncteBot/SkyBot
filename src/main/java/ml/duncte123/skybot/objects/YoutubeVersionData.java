/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;

public class YoutubeVersionData {

    /* INNERTUBE_CONTEXT_CLIENT_VERSION  x-youtube-client-version */
    private final String version;
    /* VARIANTS_CHECKSUM  x-youtube-variants-checksum */
    private final String checksum;
    /* PAGE_BUILD_LABEL  x-youtube-page-label */
    private final String label;
    /* ID_TOKEN  x-youtube-identity-token */
//    private final String idToken;
    /* PAGE_CL  x-youtube-page-cl */
    private final String pageCl;
    /* DEVICE  x-youtube-device */
//    private final String device;

    public YoutubeVersionData(String version, String checksum, String label, String pageCl) {
        this.version = version;
        this.checksum = checksum;
        this.label = label;
        this.pageCl = pageCl;
    }

    public String getVersion() {
        return version;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getLabel() {
        return label;
    }

    public String getPageCl() {
        return pageCl;
    }

    @Override
    public String toString() {
        return "YoutubeVersionData{" +
            "version='" + version + '\'' +
            ", checksum='" + checksum + '\'' +
            ", label='" + label + '\'' +
            ", pageCl='" + pageCl + '\'' +
            '}';
    }

    public static YoutubeVersionData fromBrowser(JsonBrowser json) {
        return new YoutubeVersionData(
            json.get("INNERTUBE_CONTEXT_CLIENT_VERSION").safeText(),
            json.get("VARIANTS_CHECKSUM").safeText(),
            json.get("PAGE_BUILD_LABEL").safeText(),
            json.get("PAGE_CL").safeText()
        );
    }
}
