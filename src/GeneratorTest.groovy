//package generator
//class GeneratorTest {
//
//    static void main(String[] args) {
//
////        println "args --- " + args
////
////        def pwd
////        if (System.getenv().containsKey("PWD")) {
////            pwd = System.getenv().get("PWD")
////            println "pwd --- " + pwd
////        }
////
////        def dirIn
////        if (args.size() > 0) {
////            dirIn = args[0]
////            println dirIn
////            if (dirIn.startsWith(".")) {
////                dirIn = pwd + File.separator + dirIn.substring(1)
////                File dir = new File(dirIn)
////                if (!dir.exists()) {
////                    dir.mkdir()
////                }
////                println dir.getCanonicalFile()
////                println dir.getAbsolutePath()
////            }
////        }
////
//
//
//        // println "current dir --- " + System.getProperty("user.dir")
//
//
//    }
//}