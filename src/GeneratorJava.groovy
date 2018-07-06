//import groovy.json.JsonGenerator
//
//import java.util.concurrent.CopyOnWriteArrayList
//
//class GeneratorJava {
//
//    static Map<String, MessageSimple> messageMap = new HashMap() // messageName, messageJsonBody
//
//    static void main(String[] args) {
//
//        // println args
//        if (args[0].startsWith(".")) {
//            println args[0]
//        }
//
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
//
//        parseMessage(dirProto)
//        parseToJson(dirProto, dirOutRoute)
//    }
//
//    static void generateController(File dirIn, File dirOut) {
//        // 1 遍历dirIn中proto文件
//        dirIn.eachFile {
//
//            def outFile // 定义输出文件
//            def packageName
//            def serviceName
//            def methodName
//
//            // 2 读取文件
//            def protoFileLines = it.readLines("utf-8")
//            protoFileLines.each {
//
//                if (it.trim().startsWith("package")) {
//                    packageName = it.substring(8, it.length() - 1)
//                    outFile = new File(dirOut, packageName + ".js")
//                    if (outFile.exists()) {
//                        outFile.delete()
//                    }
//                    outFile.append("\n")
//                    outFile.append("import grpc from 'grpc';\n")
//                    outFile.append("import config from '../config';\n")
//                    outFile.append("\n")
//                }
//
//                if (it.startsWith("service")) {
//                    serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//
//                    outFile.append("class " + toUpperCaseFirstOne(packageName) + "Controller {\n")
//                    outFile.append("    constructor() {\n")
//                    outFile.append("        let PROTO_PATH = __dirname + '/../protos/" + packageName + ".proto';\n")
//                    outFile.append("        let proto = grpc.load(PROTO_PATH)." + packageName + ";\n")
//                    outFile.append("        this.client =  new proto." + toUpperCaseFirstOne(serviceName) + "(config.grpc_host+':'+config.grpc_port, grpc.credentials.createInsecure());\n")
//                    outFile.append("    }\n")
//                }
//
//                if (it.trim().startsWith("rpc")) {
//                    methodName = toLowerCaseFirstC(it.trim().split(" ")[1].trim())
//                    outFile.append "    " + methodName + " = async (req, res, next) => {\n"
//                    outFile.append "        var params = {};\n"
//                    outFile.append "        for (var key in req.body){\n"
//                    outFile.append "            params[key] = req.body[key];\n"
//                    outFile.append "        }\n"
//                    outFile.append "        this.client." + methodName + "(params, function(err, response) {\n"
//                    outFile.append "            if(err) {\n"
//                    outFile.append "                next(err);\n"
//                    outFile.append "            } else {\n"
//                    outFile.append "                res.send(response);\n"
//                    outFile.append "            }\n"
//                    outFile.append "        });\n"
//                    outFile.append "    }\n"
//                }
//            }
//            if (null != outFile && outFile.exists()) {
//                outFile.append("};\n")
//                outFile.append("\n")
//                outFile.append("export default new " + toUpperCaseFirstOne(packageName) + "Controller();")
//                outFile.append("\n")
//            }
//
//            if (null == serviceName || null == methodName) {
//                if (null != outFile && outFile.exists()) {
//                    outFile.delete()
//                }
//            }
//        }
//    }
//
//    static void generateRouteFile(File dirIn, File dirOut) {
//        // 1 遍历dirIn中proto文件
//        dirIn.eachFile {
//
//            def outFile // 定义输出文件
//            def packageName
//            def serviceName
//            def methodName
//
//            // 2 读取文件
//            def protoFileLines = it.readLines("utf-8")
//            protoFileLines.each {
//
//                if (it.trim().startsWith("package")) {
//                    packageName = it.substring(8, it.length() - 1)
//                    outFile = new File(dirOut, packageName + ".js")
//                    if (outFile.exists()) {
//                        outFile.delete()
//                    }
//                    outFile.append("\n")
//                    outFile.append("import express from 'express';\n")
//                    outFile.append("import " + packageName + " from '../controller/" + packageName + "';\n") //
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
//                    methodName = toLowerCaseFirstC(it.trim().split(" ")[1].trim())
//                    outFile.append "router.post('/" + methodName + "', " + packageName + "." + methodName + ");\n"
//                }
//            }
//            if (null != outFile && outFile.exists()) {
//                outFile.append("\n")
//                outFile.append("export default router")
//                outFile.append("\n")
//            }
//
//            if (null == serviceName || null == methodName) {
//                if (null != outFile && outFile.exists()) {
//                    outFile.delete()
//                }
//            }
//        }
//    }
//
//    static void generateRouteIndexFile(File dirIn, File dirOut) {
//
//        def outFile = new File(dirOut, "index.js")
//        if (outFile.exists()) {
//            outFile.delete()
//        }
//
//        outFile.append("\n")
//        outFile.append("'use strict';\n")
//        outFile.append("\n")
//
//        dirIn.eachFile {
//            if (!(".").equals(it.name.substring(0, 1))) {
//                def fileName = it.name.substring(0, it.name.length() - 3)
//                if (!"index".equals(fileName)) {
//                    outFile.append("import " + fileName + " from './" + fileName + "';\n")
//                }
//            }
//        }
//
//        outFile.append("\n")
//        outFile.append("export default app => {\n")
//
//        dirIn.eachFile {
//            if (!(".").equals(it.name.substring(0, 1))) {
//                def fileName = it.name.substring(0, it.name.length() - 3)
//                if (!"index".equals(fileName) || it.name.substring(0, 1).equals(".")) {
//                    outFile.append("    app.use('/" + fileName + "'," + fileName + ");\n")
//                }
//            }
//        }
//
//        outFile.append("}\n")
//        outFile.append("\n")
//    }
//
//    // ==================== 首字母转小写
//    static String toLowerCaseFirstC(String s) {
//        if (Character.isLowerCase(s.charAt(0)))
//            return s;
//        else
//            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
//    }
//
//    // ==================== 首字母转大写
//    static String toUpperCaseFirstOne(String s) {
//        if (Character.isUpperCase(s.charAt(0)))
//            return s;
//        else
//            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
//    }
//
//    // ====================
//    static class MessageSimple {
//        String message
//        ArrayList<MessageProp> props
//    }
//
//    static class MessageProp {
//        String property
//        String type
//        String describe
//        ArrayList<MessageProp> props
//    }
//
//    // ====================
//    static class ServiceSimple {
//        String packageName
//        ArrayList<ServiceProp> services
//    }
//
//    static class ServiceProp {
//        String service
//        String path
//        String describe
//        def reqMessage
//        def resMessage
//    }
//
//    // ==================== to JSON
//    static void parseMessage(File dirIn) {
//        dirIn.eachFile {
//            if (it.path.contains(".")) {
//                def fullName = it.path.split("\\.")
//                if (fullName.size() > 1) {
//                    if ("proto".equals(fullName[1].trim())) {
//                        def lines = it.readLines("utf-8")
//                        CopyOnWriteArrayList list = new CopyOnWriteArrayList(lines)
//                        parseSimpleMessage(list)
//                    }
//                }
//            }
//        }
//    }
//
//    static flagStart = false
//    static flagEnd = false
//    static indexStart = 0
//    static indexEnd = 0
//    static index = 0
//
//    static void parseSimpleMessage(CopyOnWriteArrayList lines) {
//        lines.each {
//            if (it.trim().startsWith("message")) {
//                indexStart = index
//                flagStart = true
//            }
//
//            if (it.trim().startsWith("}")) {
//                if (flagStart) {
//                    indexEnd = index
//                    flagEnd = true
//                }
//            }
//
//            if (flagStart && flagEnd) {
//
//                if (indexStart >= lines.size()) {
//                    return
//                }
//
//                parseMessageBlock(lines.subList(indexStart, indexEnd + 1))
//
//                while (indexStart <= indexEnd) {
//                    lines.remove(indexStart)
//                    indexEnd--
//                }
//
//                flagStart = false
//                flagEnd = false
//                indexStart = 0
//                indexEnd = 0
//                index = 0
//
//                parseSimpleMessage(lines)
//            }
//
//            index++
//        }
//    }
//
//    static void parseMessageBlock(messageLines) {
//        def messageSingle = new MessageSimple()
//        def messageProps = new ArrayList<MessageProp>()
//
//        messageLines.each {
//            if (it.trim().startsWith("message")) {
//                def messageNameLine = it.trim().split(" ")
//                if (messageNameLine.size() >= 2) {
//                    def messageName = messageNameLine[1].trim()
//                    if ("{".equals(messageName.substring(messageName.length() - 1, messageName.length()))) {
//                        messageName = messageName.substring(0, messageName.length() - 1)
//                    }
//                    //if (messageMap.containsKey(messageName)) {
//                    //    println "有名称相同的Message定义，请修改。MessageName --- " + messageName
//                    //    Thread.currentThread().stop()
//                    //}
//                    messageSingle.message = messageName
//                }
//            } else if (it.trim().startsWith("}")) {
//                messageSingle.props = messageProps
//            } else {
//                if (it.contains(";")) {
//                    def messageProp = new MessageProp()
//
//                    if (it.trim().startsWith("repeated")) {
//                        def propLine = it.split(";")
//                        def typeAndName = propLine[0].trim().split("=")[0].trim().split(" ")
//
//                        messageProp.type = "array<" + typeAndName[1].trim() + ">"
//                        messageProp.property = typeAndName[2].trim()
//
//                        if (propLine.size() > 1) {
//                            def desc = propLine[1].trim()
//                            if (desc.contains("//")) {
//                                def start = desc.indexOf("//")
//                                messageProp.describe = desc.substring(start + 2).trim()
//                            }
//                        } else {
//                            messageProp.describe = ""
//                        }
//
//                        if (messageMap.containsKey(typeAndName[1].trim())) {
//                            messageProp.props = messageMap.get(typeAndName[1].trim()).props
//                        }
//
//                    } else {
//                        def propLine = it.split(";")
//                        def typeAndName = propLine[0].trim().split("=")[0].trim().split(" ")
//
//                        messageProp.property = typeAndName[1].trim()
//                        messageProp.type = typeAndName[0].trim()
//
//                        if (propLine.size() > 1) {
//                            def desc = propLine[1].trim()
//                            if (desc.contains("//")) {
//                                def start = desc.indexOf("//")
//                                messageProp.describe = desc.substring(start + 2).trim()
//                            }
//                        } else {
//                            messageProp.describe = ""
//                        }
//
//                        if (messageMap.containsKey(messageProp.type)) {
//                            messageProp.props = messageMap.get(messageProp.type).props
//                        }
//                    }
//
//                    messageProps.add(messageProp)
//                }
//            }
//        }
//
//        if (null != messageSingle.message && null != messageSingle.props) {
//            messageMap.put(messageSingle.message, messageSingle)
//        }
//    }
//
//    static void parseToJson(File dirIn, File dirOut) {
//        ArrayList<ServiceSimple> listServiceSimple = new ArrayList<>()
//
//        ServiceSimple serviceSimple
//        ArrayList<ServiceProp> listServiceProp
//
//        dirIn.eachFile {
//            if (it.path.contains(".")) {
//                def fullName = it.path.split("\\.")
//                if (fullName.size() > 1) {
//                    if ("proto".equals(fullName[1].trim())) {
//
//                        def protoFileLines = it.readLines("utf-8")
//
//                        // deal with package and services
//                        def packageName
//                        def serviceName
//                        serviceSimple = new ServiceSimple()
//                        listServiceProp = new ArrayList<>()
//                        ServiceProp serviceProp
//
//                        protoFileLines.each {
//                            serviceProp = new ServiceProp()
//
//                            if (it.trim().startsWith("package")) {
//                                packageName = it.substring(8, it.length() - 1)
//                                serviceSimple.packageName = packageName
//                            }
//
//                            if (it.startsWith("service")) {
//                                serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//                            }
//
//                            if (it.trim().startsWith("rpc")) {
//                                if (it.trim().contains("{}")) {
//                                    def rpcLine = it.trim().split("\\{\\}")
//                                    def pre = rpcLine[0].trim().split(" ")
//
//                                    def methodName = toLowerCaseFirstC(pre[1])
//                                    def argIn = pre[2].substring(1, pre[2].length() - 1)
//                                    def argOut = pre[4].substring(1, pre[4].length() - 1)
//
//                                    if (argIn.contains(".")) {
//                                        if (argIn.split("\\.").size() > 1) {
//                                            argIn = argIn.split("\\.")[1].trim()
//                                        }
//                                    }
//
//                                    if (argOut.contains(".")) {
//                                        if (argOut.split("\\.").size() > 1) {
//                                            argOut = argOut.split("\\.")[1].trim()
//                                        }
//                                    }
//
//                                    serviceProp.service = methodName
//                                    serviceProp.path = "/" + packageName + "/" + methodName
//                                    if (messageMap.containsKey(argIn)) {
//                                        serviceProp.reqMessage = messageMap.get(argIn)
//                                    } else {
//                                        serviceProp.reqMessage = argIn
//                                    }
//                                    if (messageMap.containsKey(argOut)) {
//                                        serviceProp.resMessage = messageMap.get(argOut)
//                                    } else {
//                                        serviceProp.resMessage = argOut
//                                    }
//
//                                    if (rpcLine.size() > 1) {
//                                        def desc = rpcLine[1].trim()
//                                        if (desc.contains("//")) {
//                                            def start = desc.indexOf("//")
//                                            serviceProp.describe = desc.substring(start + 2).trim()
//                                        }
//                                    } else {
//                                        serviceProp.describe = ""
//                                    }
//
//                                    listServiceProp.add(serviceProp)
//                                } else {
//                                    println "格式错误 --- rpc行不符合规则，请检查"
//                                    return
//                                }
//                            }
//                        }
//
//                        if (listServiceProp.size() > 0) {
//                            serviceSimple.services = listServiceProp
//                            listServiceSimple.add(serviceSimple)
//                        }
//                    }
//                }
//            }
//        }
//
//        def fileOut = new File(dirOut, "json.js")
//        if (fileOut.exists()) {
//            fileOut.delete()
//        }
//
//        def generator = new JsonGenerator.Options().excludeNulls().build()
//
//        fileOut.append("var proto = ")
//        fileOut.append(generator.toJson(listServiceSimple))
//        fileOut.append(";")
//        fileOut.append("\n")
//        fileOut.append("export default proto;")
//    }
//}