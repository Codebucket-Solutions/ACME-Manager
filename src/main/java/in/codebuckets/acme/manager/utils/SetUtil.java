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

package in.codebuckets.acme.manager.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SetUtil {

    /**
     * Create a new set with the given elements added to the original set
     *
     * @param set      The original set
     * @param elements The elements to add to the original set
     * @param <T>      The type of the elements
     * @return A new set with the given elements added to the original set
     */
    @SafeVarargs
    public static <T> Set<T> newSet(Set<T> set, T... elements) {
        // Create a new set to avoid modifying the original set
        Set<T> newSet = new HashSet<>(set);

        // Add the elements to the new set
        newSet.addAll(Arrays.asList(elements));

        // Return the new set
        return newSet;
    }
}
