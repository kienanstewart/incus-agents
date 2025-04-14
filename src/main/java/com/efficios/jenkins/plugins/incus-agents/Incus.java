/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.slaves.Cloud;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

public class Incus extends Cloud {

    private String authType;
    private String certificate;
    private String project;
    private String protocol;
    private String url;

    @DataBoundConstructor
    public Incus(String name, String authType, String certificate, String project, String protocol, String url) {
        super(name);
        this.authType = authType;
        this.certificate = certificate;
        this.project = project;
        this.protocol = protocol;
        this.url = url;
    }

    @Override
    public boolean canProvision(Label label) {
        return false;
    }

    public String getAuthType() {
        return this.authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getCertificate() {
        return this.certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Cloud> {
        @Override
        public String getDisplayName() {
            return "Incus Remote";
        }

        public ListBoxModel doFillAuthTypeItems() {
            return new ListBoxModel().add("tls").add("oidc");
        }

        public ListBoxModel doFillProtocolItems() {
            return new ListBoxModel().add("incus").add("simplestreams");
        }
    }
}
