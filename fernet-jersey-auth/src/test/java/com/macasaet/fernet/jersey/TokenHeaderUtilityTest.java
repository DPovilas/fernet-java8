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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.security.SecureRandom;

import javax.ws.rs.NotAuthorizedException;

import org.glassfish.jersey.server.ContainerRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.macasaet.fernet.FernetKeyFactory;
import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;


public class TokenHeaderUtilityTest {

    private TokenHeaderUtility utility;
    private SecureRandom random;
    private FernetKeyFactory keyFactory;

    @Before
    public void setUp() throws Exception {
        utility = new TokenHeaderUtility();
        random = new SecureRandom();
        keyFactory = new FernetKeyFactory(random);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void verifyGetAuthorizationTokenDeserialisesBearerToken() {
        // given
        final Key key = keyFactory.generateKey();
        final Token token = Token.generate(random, key, "hello");
        final ContainerRequest request = mock(ContainerRequest.class);
        given(request.getHeaderString("Authorization")).willReturn("Bearer " + token.serialise());

        // when
        final Token result = utility.getAuthorizationToken(request);

        // then
        assertEquals(token.serialise(), result.serialise());
    }

    @Test
    public final void verifyGetAuthorizationTokenRejectsMalformedHeader() {
        // given
        final ContainerRequest request = mock(ContainerRequest.class);
        given(request.getHeaderString("Authorization")).willReturn("Basic YWxpY2U6cGFzc3dvcmQ= 76bd6d14-0148-43c4-8ea0-8368336ce9f1");

        // when / then
        assertThrows(NotAuthorizedException.class, () -> utility.getAuthorizationToken(request));
    }

    @Test
    public final void verifyGetAuthorizationTokenRejectsInvalidScheme() {
        // given
        final ContainerRequest request = mock(ContainerRequest.class);
        given(request.getHeaderString("Authorization")).willReturn("Basic YWxpY2U6cGFzc3dvcmQ=");

        // when / then        
        assertThrows(NotAuthorizedException.class, () -> utility.getAuthorizationToken(request)); 
    }

    @Test
    public final void verifyGetAuthorizationTokenIgnoresX() {
        // given
        final Key key = keyFactory.generateKey();
        final Token token = Token.generate(random, key, "hello");
        final ContainerRequest request = mock(ContainerRequest.class);
        given(request.getHeaderString("X-Authorization")).willReturn(token.serialise());

        // when
        final Token result = utility.getAuthorizationToken(request);

        // then
        assertNull(result);
    }

    @Test
    public final void verifyGetXAuthorizationTokenDeserialisesToken() {
        // given
        final Key key = keyFactory.generateKey();
        final Token token = Token.generate(random, key, "hello");
        final ContainerRequest request = mock(ContainerRequest.class);
        given(request.getHeaderString("X-Authorization")).willReturn(token.serialise());

        // when
        final Token result = utility.getXAuthorizationToken(request);

        // then
        assertEquals(token.serialise(), result.serialise());
    }

    @Test
    public final void verifyGetXAuthorizationTokenIgnoresBearer() {
        // given
        final Key key = keyFactory.generateKey();
        final Token token = Token.generate(random, key, "hello");
        final ContainerRequest request = mock(ContainerRequest.class);
        given(request.getHeaderString("Authorization")).willReturn("Bearer " + token.serialise());

        // when
        final Token result = utility.getXAuthorizationToken(request);

        // then
        assertNull(result);
    }

}