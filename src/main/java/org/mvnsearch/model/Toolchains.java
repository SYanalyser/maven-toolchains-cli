package org.mvnsearch.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "toolchains")
public class Toolchains {
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Toolchain> toolchain = new ArrayList<>();

    public List<Toolchain> getToolchain() {
        return toolchain;
    }

    public void setToolchain(List<Toolchain> toolchain) {
        this.toolchain = toolchain;
    }
}
