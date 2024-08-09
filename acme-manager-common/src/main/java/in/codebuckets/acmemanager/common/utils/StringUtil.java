/*
 *    Copyright 2024, Codebucket Solutions Private Limited
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package in.codebuckets.acmemanager.common.utils;

public final class StringUtil {

    private StringUtil() {
        // private constructor to prevent instantiation
    }

    /**
     * Masks the given string by replacing the first 4 and last 4 characters with asterisks.
     *
     * @param str String to mask
     * @return Masked string
     */
    public static String maskString(String str) {
        if (str == null || str.length() < 8) {
            return str; // not enough characters to mask
        }
        return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
    }
}
