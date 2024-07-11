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

package in.codebuckets.acmemanager.server.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationRequestRepository extends JpaRepository<ValidationRequest, Long> {

    /**
     * Find all {@link ValidationRequest} by Order URL
     *
     * @param orderUrl Order URL to search for
     * @return List of {@link ValidationRequest}
     */
    List<ValidationRequest> findAllByOrderUrl(String orderUrl);

    List<ValidationRequest> findAllByOrderId(String orderId);
}
