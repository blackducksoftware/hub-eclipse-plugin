/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.eclipse.internal;

import com.blackducksoftware.integration.rest.credentials.Credentials;
import com.blackducksoftware.integration.rest.credentials.CredentialsBuilder;
import com.blackducksoftware.integration.rest.credentials.CredentialsValidator;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class DecryptedCredentialsBuilder extends CredentialsBuilder {
    private String username;
    private String password;

    @Override
    public Credentials buildObject() {
        return new Credentials(username, password, 0);
    }

    @Override
    public AbstractValidator createValidator() {
        final CredentialsValidator validator = new CredentialsValidator();
        validator.setUsername(getUsername());
        validator.setPassword(getPassword());
        return validator;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public int getPasswordLength() {
        return 0;
    }

}
