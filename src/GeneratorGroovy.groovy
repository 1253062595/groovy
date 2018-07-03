//class GeneratorGroovy {
//
//    static void main(String[] args) {
//
//        // ==================== 命令行执行
////        def pathIn
////        def pathOutJS
////        def pathOutCLS
////        def pathOutIndex
////
////        if (null == args || args.size() < 1) {
////            println "请在命令行尾部输入proto文件夹路径、生成的js文件夹路径、生成的class文件夹路径 及 生成的index文件夹路径"
////            return
////        }
////
////        if (null != args) {
////            if (args.size() >= 1) {
////                pathIn = args[0]
////            }
////            if (args.size() >= 2) {
////                pathOutJS = args[1]
////            }
////            if (args.size() >= 3) {
////                pathOutCLS = args[2]
////            }
////            if (args.size() >= 4) {
////                pathOutIndex = args[3]
////            }
////        }
////
////        def dirIn
////        def dirOutJS
////        def dirOutCLS
////        def dirOutIndex
////
////        if (null != pathIn) {
////            dirIn = new File(pathIn)
////            if (!dirIn.exists()) {
////                dirIn.mkdir()
////            }
////        } else {
////            println "未输入proto文件夹路径"
////            return
////        }
////
////        if (null != pathOutJS) {
////            dirOutJS = new File(pathOutJS)
////            if (!dirOutJS.exists()) {
////                dirOutJS.mkdir()
////            }
////        } else {
////            println "未输入生成js文件夹路径"
////            return
////        }
////
////        if (null != pathOutCLS) {
////            dirOutCLS = new File(pathOutCLS)
////            if (!dirOutCLS.exists()) {
////                dirOutCLS.mkdir()
////            }
////        } else {
////            println "未输入生成class文件夹路径"
////            return
////        }
////
////        if (null != pathOutIndex) {
////            dirOutIndex = new File(pathOutIndex)
////            if (!dirOutIndex.exists()) {
////                dirOutIndex.mkdir()
////            }
////        } else {
////            println "未输入生成index文件夹路径"
////            return
////        }
//
//        // ==================== IDE执行
//        def dirIn = new File("/Users/jys/groovy/projects/p2js/dirIn")
//        def dirOutJS = new File("/Users/jys/groovy/projects/p2js/dirOut1")
//        def dirOutCLS = new File("/Users/jys/groovy/projects/p2js/dirOut2")
//        def dirOutIndex = new File("/Users/jys/groovy/projects/p2js/dirOut3")
//
//        // ====================
//        generateJSFile(dirIn, dirOutJS)
//        generateCLSFile(dirIn, dirOutCLS)
//        generateIndexFile(dirIn, dirOutIndex)
//    }
//
//    // ====================
//    static String generateJSFile(File dirIn, File dirOut) {
//        goThroughDir(dirIn) {
//            // 定义输出文件
//            def outFile
//            def packageName
//            def serviceName
//
//            // 2 读取文件
//            def protoFileLines = it.readLines("utf-8")
//            goThroughFile(protoFileLines) {
//                if (it.trim().startsWith("package")) {
//                    packageName = it.substring(8, it.length() - 1)
//                    outFile = new File(dirOut, packageName + ".js")
//                    if (outFile.exists()) {
//                        outFile.delete()
//                    }
//                    outFile.append("\n")
//                    outFile.append("import express from 'express';\n")
//                    outFile.append("import " + packageName + " from '../controller/" + packageName + "';\n")
//                    outFile.append("\n")
//                    outFile.append("const router = express.Router();\n")
//                    outFile.append("\n")
//                }
//
//                if (it.startsWith("service")) {
//                    serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//                }
//
//                if (it.trim().startsWith("rpc")) {
//                    def methodName = toLowerCaseFirstC(it.trim().split(" ")[1].trim())
//                    outFile.append "router.post('/" + methodName + "', " + packageName + "." + methodName + ");\n"
//                }
//            }
//            outFile.append("\n")
//            outFile.append("export default router")
//            outFile.append("\n")
//        }
//    }
//
//    static void generateCLSFile(File dirIn, File dirOut) {
//        // 1 遍历dirIn中proto文件
//        goThroughDir(dirIn) {
//
//            // 定义输出文件
//            def outFile
//            def packageName
//            def serviceName
//
//            // 2 读取文件
//            def protoFileLines = it.readLines("utf-8")
//            goThroughFile(protoFileLines) {
//
//                if (it.trim().startsWith("package")) {
//                    packageName = it.substring(8, it.length() - 1)
//                    outFile = new File(dirOut, packageName + ".js")
//                    if (outFile.exists()) {
//                        outFile.delete()
//                    }
//                    outFile.append("\n") // 此行固定
//                    outFile.append("import grpc from 'grpc';\n") // 此行固定
//                    outFile.append("\n") // 此行固定
//                    outFile.append("let PROTO_PATH = __dirname + '/../protos/" + packageName + ".proto';\n")
//                    outFile.append("let proto = grpc.load(PROTO_PATH)." + packageName + ";\n")
//                }
//
//                if (it.startsWith("service")) {
//                    serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//                    outFile.append("let client =  new proto." + toUpperCaseFirstOne(serviceName) + "('localhost:50051', grpc.credentials.createInsecure());\n")
//                    outFile.append("class " + toUpperCaseFirstOne(packageName) + "Controller {\n")
//                    outFile.append("    constructor() {\n")
//                    outFile.append("    }\n")
//                }
//
//                if (it.trim().startsWith("rpc")) {
//                    def methodName = toLowerCaseFirstC(it.trim().split(" ")[1].trim())
//                    outFile.append "    async " + methodName + " (req, res, next) {\n"
//                    outFile.append "        var params = {};\n"
//                    outFile.append "        for (var key in req.body){\n"
//                    outFile.append "            params[key] = req.body[key];\n"
//                    outFile.append "        }\n"
//                    outFile.append "        client." + methodName + "(params, function(err, response) {\n"
//                    outFile.append "            if(err) {\n"
//                    outFile.append "                next(err);\n"
//                    outFile.append "            } else {\n"
//                    outFile.append "                res.send(response);\n"
//                    outFile.append "            }\n"
//                    outFile.append "        });\n"
//                    outFile.append "    }\n"
//                }
//            }
//            outFile.append("};\n") // 此行固定
//            outFile.append("\n") // 此行固定
//            outFile.append("export default new " + toUpperCaseFirstOne(packageName) + "Controller();")
//            outFile.append("\n") // 此行固定
//        }
//    }
//
//    static void generateIndexFile(File dirIn, File dirOut) {
//        def outFile = new File(dirOut, "index.js")
//        if (outFile.exists()) {
//            outFile.delete()
//        }
//
//        outFile.append("\n")
//        outFile.append("'use strict';\n")
//        outFile.append("\n")
//
//        goThroughDir(dirIn) {
//            def fileName = it.name.substring(0, it.name.length() - 6)
//            outFile.append("import " + fileName + " from './" + fileName + "';\n")
//        }
//
//        outFile.append("\n")
//        outFile.append("export default app => {\n")
//
//        goThroughDir(dirIn) {
//            def fileName = it.name.substring(0, it.name.length() - 6)
//            outFile.append("    app.use('/" + fileName + "'," + fileName + ");\n")
//        }
//
//        outFile.append("}\n")
//        outFile.append("\n")
//    }
//
//    // ====================
//    // 遍历文件夹中文件
//    def static goThroughDir(dirIn, block) {
//        dirIn.eachFile {
//            block(it)
//        }
//    }
//
//    // 遍历文件每一行
//    def static goThroughFile(lines, block) {
//        lines.each {
//            block(it)
//        }
//    }
//
//    // ====================
//    //首字母转小写
//    static String toLowerCaseFirstC(String s) {
//        if (Character.isLowerCase(s.charAt(0)))
//            return s;
//        else
//            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
//    }
//
//    //首字母转大写
//    static String toUpperCaseFirstOne(String s) {
//        if (Character.isUpperCase(s.charAt(0)))
//            return s;
//        else
//            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
//    }
//}
