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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import in.codebuckets.acmemanager.server.dto.Challenge;
import io.hypersistence.utils.hibernate.id.Tsid;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.shredzone.acme4j.Status;

import java.time.Instant;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "validation_requests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class ValidationRequest {

    @Tsid
    @Id
    @JsonProperty
    private Long id;

    @Column(length = 512, updatable = false)
    @JsonProperty
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column
    @JsonProperty
    private Status status;

    @JsonFormat(shape = STRING, timezone = "UTC")
    @Column(updatable = false)
    @JsonProperty
    @CreationTimestamp
    private Instant createdAt;

    @JsonFormat(shape = STRING, timezone = "UTC")
    @Column(updatable = false)
    @JsonProperty
    private Instant expiresAt;

    @Column(length = 1024)
    @JsonProperty
    private String orderUrl;

    @Column(length = 512)
    @JsonProperty
    private String orderId;

    @JsonProperty
    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<Challenge> challenges;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(referencedColumnName = "orderId")
    @JsonProperty
    private Certificate certificate;

    public void status(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ValidationRequest{" +
                "id=" + id +
                ", domain='" + domain + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", orderUrl='" + orderUrl + '\'' +
                ", orderId='" + orderId + '\'' +
                ", challenges=" + challenges +
                ", certificate=" + certificate.orderId() +
                '}';
    }
}
