package org.mvnsearch.commands;

import org.mvnsearch.model.Toolchain;
import org.mvnsearch.service.FoojayService;
import org.mvnsearch.service.JdkDownloadLink;
import org.mvnsearch.service.ToolchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.ProcessExecutor;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Add JDK to toolchains.xml
 *
 * @author linux_china
 */
@Component
@CommandLine.Command(name = "add", mixinStandardHelpOptions = true, description = "Add JDK into toolchains.xml from local or remote")
public class AddJDK implements Callable<Integer>, BaseCommand {
    @CommandLine.Option(names = "--vendor", description = "Java Vendor name: temurin(default), oracle_openjdk, graalvm_ce17")
    private String vendor = "temurin";
    @CommandLine.Parameters(index = "0", description = "Java version: 8, 11, 17")
    private String version;
    @CommandLine.Parameters(index = "1", arity = "0..1", description = "Local Java Home with absolute path")
    private String javaHome;
    @Autowired
    private FoojayService foojayService;
    @Autowired
    private ToolchainService toolchainService;

    @Override
    public Integer call() {
        Toolchain toolchain = toolchainService.findToolchain(version, vendor);
        if (toolchain == null) {
            // link local jdk to toolchains.xml
            if (javaHome != null) {
                // check home for Mac
                if (new File(javaHome, "Contents/Home").exists()) {
                    javaHome = new File(javaHome, "Contents/Home").getAbsolutePath();
                }
                return installFromLocal();
            }
            try {
                JdkDownloadLink download = foojayService.findRelease(version, vendor);
                if (download != null) {
                    File jdksDir = new File(System.getProperty("user.home") + "/.m2/jdks");
                    File jdkHome = foojayService.downloadAndExtract(download.getUrl(), download.getFileName(), jdksDir.getAbsolutePath());
                    if (new File(jdkHome, "Contents/Home").exists()) {  // mac tgz
                        jdkHome = new File(jdkHome, "Contents/Home");
                    }
                    toolchainService.addToolChain(version, vendor, jdkHome.getAbsolutePath());
                    System.out.println("Succeed to add JDK " + version + " in toolchains.xml");
                    if (vendor.contains("graalvm")) {
                        File guBin = new File(jdkHome, "bin/gu");
                        new ProcessExecutor()
                                .environment("GRAALVM_HOME", jdkHome.getAbsolutePath())
                                .command(guBin.getAbsolutePath(), "install", "native-image", "--ignore")
                                .execute();
                    }
                } else {
                    System.out.println("JDK not found: " + version);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to fetch information from Adoptium https://adoptium.net/!");
            }
        } else {
            System.out.println("Added already!");
        }
        return 0;
    }


    private int installFromLocal() {
        File javaBin = new File(javaHome, "bin/java.exe");
        if (javaBin.exists()) {
            toolchainService.addToolChain(version, vendor, javaHome);
            System.out.println("Succeed to add JDK " + version + " in toolchains.xml");
            return 0;
        } else {
            System.out.println("Java Home is not correct: " + javaHome);
            return 1;
        }
    }

}