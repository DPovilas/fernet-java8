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
package com.macasaet.fernet.jersey.tokeninjection;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;


public class TokenInjectionIT extends JerseyTest {

    private final Validator<String> validator = new StringValidator() {
    };

    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ExampleTokenInjectionApplication();
    }

    @Test
    public final void test() {
        // given
        final Random random = new Random();
        final Key key = Key.generateKey(random);
        final Token token = Token.generate(random, key, "Hello!");
        final Invocation request = this.target().path("resource").request().header("X-Authorization", token.serialise()).buildGet();

        // when
        final String result = request.invoke(String.class);

        // then
        final Token echoedToken = Token.fromString(result);
        final String message = echoedToken.validateAndDecrypt(key, validator);
        assertEquals("Hello!", message);
    }

}