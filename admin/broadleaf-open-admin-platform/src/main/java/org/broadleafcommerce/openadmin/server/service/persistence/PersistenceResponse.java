/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.openadmin.server.service.persistence;

import org.broadleafcommerce.openadmin.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.dto.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeff Fischer
 */
public class PersistenceResponse {

    protected DynamicResultSet dynamicResultSet;
    protected Entity entity;
    protected Map<String, Object> additionalData = new HashMap<String, Object>();

    public PersistenceResponse withDynamicResultSet(DynamicResultSet dynamicResultSet) {
        setDynamicResultSet(dynamicResultSet);
        return this;
    }

    public PersistenceResponse withEntity(Entity entity) {
        setEntity(entity);
        return this;
    }

    public PersistenceResponse withAdditionalData(Map<String, Object> additionalData) {
        setAdditionalData(additionalData);
        return this;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    public DynamicResultSet getDynamicResultSet() {
        return dynamicResultSet;
    }

    public void setDynamicResultSet(DynamicResultSet dynamicResultSet) {
        this.dynamicResultSet = dynamicResultSet;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public static class AdditionalData {
        public static final String CLONEID = "cloneId";
    }
}