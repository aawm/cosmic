// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Automatically generated by addcopyright.py at 04/03/2012
package com.cloud.resourcedetail;

import com.cloud.api.ResourceDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "load_balancer_healthcheck_policy_details")
public class LBHealthCheckPolicyDetailVO implements ResourceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "lb_policy_id")
    private long resourceId;

    @Column(name = "name")
    private String name;

    @Column(name = "value", length = 1024)
    private String value;

    @Column(name = "display")
    private boolean display = true;

    public LBHealthCheckPolicyDetailVO() {
    }

    public LBHealthCheckPolicyDetailVO(final long id, final String name, final String value, final boolean display) {
        this.resourceId = id;
        this.name = name;
        this.value = value;
        this.display = display;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getResourceId() {
        return resourceId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }
}
