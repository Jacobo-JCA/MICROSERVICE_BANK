package com.sofka.accounts.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.identity.H2IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupport;

public class H2TestDialect extends H2Dialect {

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new H2IdentityColumnSupport() {
            @Override
            public boolean supportsInsertSelectIdentity() {
                return false;
            }
        };
    }
}
