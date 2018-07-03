import groovy.json.JsonBuilder
import groovy.json.JsonOutput

class GeneratorJava {

    static void main(String[] args) {

//        // ====================
//        def pathProto
//        def pathOutController
//        def pathOutRoute
//
//        if (null == args || args.size() < 3) {
//            println "请在命令行尾部输入proto文件夹路径、生成的controller文件夹路径 及 生成的route文件夹路径"
//            return
//        }
//
//        if (null != args) {
//            if (args.size() >= 1) {
//                pathProto = args[0]
//            }
//            if (args.size() >= 2) {
//                pathOutController = args[1]
//            }
//            if (args.size() >= 3) {
//                pathOutRoute = args[2]
//            }
//        }
//
//        def dirProto
//        def dirOutController
//        def dirOutRoute
//
//        if (null != pathProto) {
//            dirProto = new File(pathProto)
//            if (!dirProto.exists()) {
//                dirProto.mkdir()
//            }
//        } else {
//            println "未输入proto文件夹路径"
//            return
//        }
//
//        if (null != pathOutController) {
//            dirOutController = new File(pathOutController)
//            if (dirOutController.exists()) {
//                dirOutController.delete()
//            }
//            if (!dirOutController.exists()) {
//                dirOutController.mkdir()
//            }
//        } else {
//            println "未输入controller文件夹路径"
//            return
//        }
//
//        if (null != pathOutRoute) {
//            dirOutRoute = new File(pathOutRoute)
//            if (dirOutRoute.exists()) {
//                dirOutRoute.delete()
//            }
//            if (!dirOutRoute.exists()) {
//                dirOutRoute.mkdir()
//            }
//        } else {
//            println "未输入route文件夹路径"
//            return
//        }
//
//        // ====================
//        generateController(dirProto, dirOutController)
//        generateRouteFile(dirProto, dirOutRoute)
//        generateRouteIndexFile(dirOutRoute, dirOutRoute)

        Map<String, String> messageMap = new HashMap() // messageName, messageJsonBody

        def dirIn = new File("/Users/jys/groovy/projects/p2js/dirIn")
        parseToJson(dirIn)

    }

    static void generateController(File dirIn, File dirOut) {
        // 1 遍历dirIn中proto文件
        dirIn.eachFile {

            def outFile // 定义输出文件
            def packageName
            def serviceName
            def methodName

            // 2 读取文件
            def protoFileLines = it.readLines("utf-8")
            protoFileLines.each {

                if (it.trim().startsWith("package")) {
                    packageName = it.substring(8, it.length() - 1)
                    outFile = new File(dirOut, packageName + ".js")
                    if (outFile.exists()) {
                        outFile.delete()
                    }
                    outFile.append("\n")
                    outFile.append("import grpc from 'grpc';\n")
                    outFile.append("import config from '../config';\n")
                    outFile.append("\n")
                }

                if (it.startsWith("service")) {
                    serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))

                    outFile.append("class " + toUpperCaseFirstOne(packageName) + "Controller {\n")
                    outFile.append("    constructor() {\n")
                    outFile.append("        let PROTO_PATH = __dirname + '/../protos/" + packageName + ".proto';\n")
                    outFile.append("        let proto = grpc.load(PROTO_PATH)." + packageName + ";\n")
                    outFile.append("        this.client =  new proto." + toUpperCaseFirstOne(serviceName) + "(config.grpc_host+':'+config.grpc_port, grpc.credentials.createInsecure());\n")
                    outFile.append("    }\n")
                }

                if (it.trim().startsWith("rpc")) {
                    methodName = toLowerCaseFirstC(it.trim().split(" ")[1].trim())
                    outFile.append "    " + methodName + " = async (req, res, next) => {\n"
                    outFile.append "        var params = {};\n"
                    outFile.append "        for (var key in req.body){\n"
                    outFile.append "            params[key] = req.body[key];\n"
                    outFile.append "        }\n"
                    outFile.append "        this.client." + methodName + "(params, function(err, response) {\n"
                    outFile.append "            if(err) {\n"
                    outFile.append "                next(err);\n"
                    outFile.append "            } else {\n"
                    outFile.append "                res.send(response);\n"
                    outFile.append "            }\n"
                    outFile.append "        });\n"
                    outFile.append "    }\n"
                }
            }
            if (null != outFile && outFile.exists()) {
                outFile.append("};\n")
                outFile.append("\n")
                outFile.append("export default new " + toUpperCaseFirstOne(packageName) + "Controller();")
                outFile.append("\n")
            }

            if (null == serviceName || null == methodName) {
                if (null != outFile && outFile.exists()) {
                    outFile.delete()
                }
            }
        }
    }

    static void generateRouteFile(File dirIn, File dirOut) {
        // 1 遍历dirIn中proto文件
        dirIn.eachFile {

            def outFile // 定义输出文件
            def packageName
            def serviceName
            def methodName

            // 2 读取文件
            def protoFileLines = it.readLines("utf-8")
            protoFileLines.each {

                if (it.trim().startsWith("package")) {
                    packageName = it.substring(8, it.length() - 1)
                    outFile = new File(dirOut, packageName + ".js")
                    if (outFile.exists()) {
                        outFile.delete()
                    }
                    outFile.append("\n")
                    outFile.append("import express from 'express';\n")
                    outFile.append("import " + packageName + " from '../controller/" + packageName + "';\n") //
                    outFile.append("\n")
                    outFile.append("const router = express.Router();\n")
                    outFile.append("\n")
                }

                if (it.startsWith("service")) {
                    serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
                }

                if (it.trim().startsWith("rpc")) {
                    methodName = toLowerCaseFirstC(it.trim().split(" ")[1].trim())
                    outFile.append "router.post('/" + methodName + "', " + packageName + "." + methodName + ");\n"
                }
            }
            if (null != outFile && outFile.exists()) {
                outFile.append("\n")
                outFile.append("export default router")
                outFile.append("\n")
            }

            if (null == serviceName || null == methodName) {
                if (null != outFile && outFile.exists()) {
                    outFile.delete()
                }
            }
        }
    }

    static void generateRouteIndexFile(File dirIn, File dirOut) {

        def outFile = new File(dirOut, "index.js")
        if (outFile.exists()) {
            outFile.delete()
        }

        outFile.append("\n")
        outFile.append("'use strict';\n")
        outFile.append("\n")

        dirIn.eachFile {
            if (!(".").equals(it.name.substring(0, 1))) {
                def fileName = it.name.substring(0, it.name.length() - 3)
                if (!"index".equals(fileName)) {
                    outFile.append("import " + fileName + " from './" + fileName + "';\n")
                }
            }
        }

        outFile.append("\n")
        outFile.append("export default app => {\n")

        dirIn.eachFile {
            if (!(".").equals(it.name.substring(0, 1))) {
                def fileName = it.name.substring(0, it.name.length() - 3)
                if (!"index".equals(fileName) || it.name.substring(0, 1).equals(".")) {
                    outFile.append("    app.use('/" + fileName + "'," + fileName + ");\n")
                }
            }
        }

        outFile.append("}\n")
        outFile.append("\n")
    }

    //首字母转小写
    static String toLowerCaseFirstC(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    //首字母转大写
    static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    static void parseToJson(File dirIn) {
        // 1 遍历dirIn中proto文件
        dirIn.eachFile {

            def flagStart = false // 本message开始标记
            def flagEnd = false // 本message结束标记
            def indexStart = 0 // message start index
            def indexEnd = 0 // message end index
            def index = 0

            // 2 读取文件
            def protoFileLines = it.readLines("utf-8")
            protoFileLines.each {

                // 规则---repeated的message全部写外部，即message不包含message，并且repeated的message写在包含它的message的前边
                // 开始
                if (it.startsWith("message")) {
                    indexStart = index
                    flagStart = true
                }

                // 结束
                if (it.startsWith("}")) {
                    if (flagStart) {
                        indexEnd = index
                        flagEnd = true
                    }
                }

                if (flagStart && flagEnd) {
                    parseMessageBlock(protoFileLines.subList(indexStart, indexEnd + 1))
                    flagStart = false
                    flagEnd = false
                    indexStart = 0
                    indexEnd = 0
                }

                index++
            }
        }
    }

    static void parseMessageBlock(messageLines) {

        def messageSingle = new MessageSingle()
        def messageProps = new ArrayList<MessageProp>()

        messageLines.each {
            if (it.trim().startsWith("message")) {
                // 开始
                def messageNameLine = it.split(" ")
                if (messageNameLine.size() >= 2) {
                    messageSingle.message = messageNameLine[1].trim()
                }
            } else if (it.trim().startsWith("}")) {
                // 结束
                messageSingle.props = messageProps
            } else {
                // 中间 属性
                if (it.contains(";")) {
                    def messageProp = new MessageProp()

                    def propLine = it.split(";")
                    def typeAndName = propLine[0].trim().split("=")[0].trim().split(" ")

                    messageProp.property = typeAndName[1].trim()
                    messageProp.type = typeAndName[0].trim()

                    if (propLine.size() > 1) {
                        def desc = propLine[1].trim()
                        if (desc.contains("//")) {
                            def start = desc.indexOf("//")
                            messageProp.describe = desc.substring(start + 2).trim()
                        }
                    } else {
                        messageProp.describe = ""
                    }

                    messageProps.add(messageProp)
                }
            }
        }

        if (null != messageSingle.message && null != messageSingle.props) {
            def json = JsonOutput.toJson(messageSingle)
            println "json string --- " + json
        }
    }
}