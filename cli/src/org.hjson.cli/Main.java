package org.hjson.cli;

import org.hjson.*;
import java.io.*;
import static java.lang.System.out;
import org.apache.commons.cli.*;

import java.lang.reflect.*;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

public class Main {

  public static String convertStreamToString(InputStream is) throws IOException {
    Writer writer=new StringWriter();
    char[] buffer=new char[1024];
    try {
      Reader reader=new BufferedReader(new InputStreamReader(is, "UTF-8"));
      int n;
      while ((n=reader.read(buffer))!=-1) writer.write(buffer, 0, n);
    } finally {
      is.close();
    }
    return writer.toString();
  }

  public static void main(String[] args) throws Exception {

    Options options=new Options();
    options.addOption(Option.builder("h").longOpt("help").desc("Show this screen").build());
    options.addOption(Option.builder("j").desc("Output as formatted JSON").build());
    options.addOption(Option.builder("c").desc("Output as JSON").build());
    options.addOption(Option.builder("v").longOpt("version").desc("Show version").build());
    options.addOption(Option.builder().longOpt("math").desc("Enable math DSF (experimental)").build());
    options.addOption(Option.builder().longOpt("hex").desc("Enable hex DSF (experimental)").build());

    HjsonOptions opt=new HjsonOptions();

    CommandLineParser parser=new DefaultParser();
    HelpFormatter formatter=new HelpFormatter();
    CommandLine cmd;

    try {
      cmd=parser.parse(options, args);

      List<IHjsonDsfProvider> dsf=new ArrayList<IHjsonDsfProvider>();

      if (cmd.hasOption("hex")) dsf.add(HjsonDsf.hex(false));
      if (cmd.hasOption("math")) dsf.add(HjsonDsf.math());
      opt.setDsfProviders(dsf.toArray(new IHjsonDsfProvider[0]));

      if (cmd.hasOption("h")) {
        formatter.printHelp("hjson [INPUT]", options);
        return;
      } else if (cmd.hasOption("v")) {
        String className=JsonObject.class.getSimpleName()+".class";
        String classPath=JsonObject.class.getResource(className).toString();
        if (!classPath.startsWith("jar")) return;
        String manifestPath=classPath.substring(0, classPath.lastIndexOf("!")+1)+"/META-INF/MANIFEST.MF";
        Attributes attr=new Manifest(new URL(manifestPath).openStream()).getMainAttributes();
        out.println("hjson v"+attr.getValue("Hjson-Version"));
        return;
      }

      List<String> cargs=cmd.getArgList();
      Reader reader;

      if (cargs.size()>1) throw new org.apache.commons.cli.ParseException("input");

      if (cargs.isEmpty()) reader=new InputStreamReader(System.in);
      else reader=new FileReader(cargs.get(0));
      JsonValue value=JsonValue.readHjson(reader, opt);

      if (cmd.hasOption("j")) out.println(value.toString(Stringify.FORMATTED));
      else if (cmd.hasOption("c")) out.println(value.toString(Stringify.PLAIN));
      else out.println(value.toString(opt));

    } catch (org.apache.commons.cli.ParseException e) {
      out.println(e.getMessage());
      formatter.printHelp("hjson [INPUT]", options);
      System.exit(1);
    }
  }
}
