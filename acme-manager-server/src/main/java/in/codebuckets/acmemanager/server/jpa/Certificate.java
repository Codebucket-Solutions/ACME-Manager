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
import in.codebuckets.acmemanager.server.acme.AcmeProvider;
import io.hypersistence.utils.hibernate.id.Tsid;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "certificates")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class Certificate {

    @Tsid
    @Id
    @JsonProperty
    private Long id;

    @JsonProperty
    @Column(unique = true, updatable = false)
    private String orderId;

    @Column(length = 1024)
    @JsonProperty
    private String orderUrl;

    @Column
    @JsonProperty
    private boolean saveKeyPair;

    @ManyToOne(fetch = EAGER)
    @JoinColumn
    @JsonProperty
    private Account account;

    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    private AcmeProvider acmeProvider;

    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    private CertificateStatus certificateStatus;

    @JsonProperty
    @Type(JsonType.class)
    @Column(columnDefinition = "json", updatable = false)
    private List<String> domains;

    @OneToMany(mappedBy = "certificate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty
    private List<ValidationRequest> validationRequests;

    @JsonFormat(shape = STRING, timezone = "UTC")
    @Column(updatable = false)
    @JsonProperty
    @CreationTimestamp
    private Instant createdAt;

    @JsonFormat(shape = STRING, timezone = "UTC")
    @Column
    @JsonProperty
    @UpdateTimestamp
    private Instant updatedAt;

    public void certificateStatus(CertificateStatus certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "id=" + id() +
                ", orderId='" + orderId() + '\'' +
                ", orderUrl='" + orderUrl() + '\'' +
                ", saveKeyPair=" + saveKeyPair() +
                ", account=" + account().id() +
                ", acmeProvider=" + acmeProvider() +
                ", certificateStatus=" + certificateStatus() +
                ", domains=" + domains() +
                ", validationRequests=" + validationRequests +
                ", createdAt=" + createdAt() +
                ", updatedAt=" + updatedAt() +
                '}';
    }
}
