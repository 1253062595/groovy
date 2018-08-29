//import groovy.lang.Binding;
//import groovy.lang.GroovyShell;
//import groovy.lang.Script;
//import groovy.util.GroovyScriptEngine;
//
//import java.io.File;
//
//public class JavaUseGroovy {
//
//    public static void main(String[] args) {
//
//        String dirScriptPath = System.getProperty("user.dir") + File.separator + "src";
//
////        try {
////            GroovyScriptEngine engine = new GroovyScriptEngine(dirScriptPath);
////
////            // 1 执行某个脚本文件，可附带参数
////            Binding binding = new Binding();
////            binding.setVariable("var1", "var-1");
////            // engine.run("GeneratorJava.groovy", binding);
////
////            String result = engine.run("GeneratorJava.groovy", "123");
////            System.out.println("result is " + result);
////
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//
//        GroovyShell groovyShell = new GroovyShell();
//        try {
//            File file = new File(dirScriptPath, "GeneratorJava.groovy");
//            Script script = groovyShell.parse(file);
//            Object[] argss = new Object[]{};
//            script.invokeMethod("testJavaCallGroovy", null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//}
