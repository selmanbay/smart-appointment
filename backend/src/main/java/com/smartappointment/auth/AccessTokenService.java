package com.smartappointment.auth;

import com.smartappointment.auth.dto.AccountResponse;
import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.security.JwtConfig;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Issues signed access tokens with the claims listed in ADR-0015. */
@Service
public class AccessTokenService {

  static final String ORGANIZATION_CLAIM = "organizationId";
  static final String STAFF_CLAIM = "staffId";

  private final JwtEncoder jwtEncoder;
  private final JwtProperties properties;
  private final Clock clock;

  public AccessTokenService(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock) {
    this.jwtEncoder = jwtEncoder;
    this.properties = properties;
    this.clock = clock;
  }

  public AccessToken issue(AccountResponse account) {
    Instant issuedAt = clock.instant();
    JwtClaimsSet.Builder claims =
        JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .subject(String.valueOf(account.userId()))
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(properties.lifetime()))
            .claim(JwtConfig.ROLE_CLAIM, account.role().name());
    if (account.organizationId() != null) {
      claims.claim(ORGANIZATION_CLAIM, account.organizationId());
    }
    if (account.staffId() != null) {
      claims.claim(STAFF_CLAIM, account.staffId());
    }
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    String value =
        jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    return new AccessToken(value, properties.lifetime());
  }
}
