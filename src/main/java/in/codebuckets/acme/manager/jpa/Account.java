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

package in.codebuckets.acme.manager.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Entity
@Table(name = "account", indexes = {
        @Index(name = "idx_account_id", columnList = "account_id", unique = true),
        @Index(name = "idx_email_address", columnList = "email_address", unique = true)
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class Account {

    @Tsid
    @JsonProperty
    @Id
    private Long id;

    @JsonProperty
    @Column(length = 512)
    private String serverUri;

    @JsonProperty
    @Column(length = 512, updatable = false, unique = true)
    private String accountId;

    @JsonProperty
    @Column(length = 512, updatable = false, unique = true)
    private String accountLocation;

    @JsonProperty
    @Column
    private String emailAddress;

    @JsonProperty
    @Column(columnDefinition = "TEXT")
    private String privateKey;

    @JsonFormat(shape = STRING, timezone = "UTC")
    @JsonProperty
    @Column
    @CreationTimestamp
    private Instant createdAt;

    @JsonFormat(shape = STRING, timezone = "UTC")
    @JsonProperty
    @Column
    @UpdateTimestamp
    private Instant updatedAt;
}
