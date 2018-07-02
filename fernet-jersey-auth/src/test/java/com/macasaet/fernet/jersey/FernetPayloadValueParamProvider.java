/**
   Copyright 2018 Carlos Macasaet

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.macasaet.fernet.jersey;

import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.glassfish.jersey.server.spi.internal.ValueParamProvider.Priority.NORMAL;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;
import com.macasaet.fernet.jaxrs.FernetSecret;

@Singleton
class FernetPayloadValueParamProvider<T> implements ValueParamProvider {

    private final TokenHeaderUtility headerUtility = new TokenHeaderUtility();
    private final Validator<T> validator;
    private final Supplier<Collection<Key>> keySupplier;

    @Inject
    public FernetPayloadValueParamProvider(final Validator<T> validator,
            final Supplier<Collection<Key>> keySupplier) {
        System.out.println("-- FernetPayloadValueParamProvider( " + validator + ", " + keySupplier + " )" );
        if (validator == null) {
            throw new IllegalArgumentException("validator cannot be null");
        }
        if (keySupplier == null) {
            throw new IllegalArgumentException("keySupplier cannot be null");
        }
        this.validator = validator;
        this.keySupplier = keySupplier;
    }

    public Function<ContainerRequest, T> getValueProvider(final Parameter parameter) {
        System.out.println("-- getValueProvider( " + parameter + " )" );
        return (request) -> {
            System.out.println("-- provideValue( " + request + ", " + parameter + " )" );
            if (parameter.isAnnotationPresent(FernetSecret.class)) {
                final Collection<? extends Key> keys = getKeySupplier().get();
                final Token xAuthorizationToken = getHeaderUtility().getXAuthorizationToken(request);
                if (xAuthorizationToken != null) {
                    return getValidator().validateAndDecrypt(keys, xAuthorizationToken);
                }
                final Token authorizationToken = getHeaderUtility().getAuthorizationToken(request);
                if (authorizationToken != null) {
                    return getValidator().validateAndDecrypt(keys, authorizationToken);
                }
                throw new WebApplicationException(status(UNAUTHORIZED).entity("missing auth header").build());
            }
            throw new IllegalStateException("misconfigured annotation");
        };
    }

    public PriorityType getPriority() {
        return NORMAL;
    }

    protected Validator<T> getValidator() {
        return validator;
    }

    protected Supplier<? extends Collection<? extends Key>> getKeySupplier() {
        return keySupplier;
    }

    protected TokenHeaderUtility getHeaderUtility() {
        return headerUtility;
    }

}