//import groovy.json.JsonGenerator
//
//import java.util.concurrent.CopyOnWriteArrayList
//
//class GeneratorJava {
//
//    static Map<String, MessageSimple> messageMap = new HashMap()
//
//    static void main(String[] args) {
//
//        String rootDir = System.getProperty("user.dir")
//        File dirProto = new File(rootDir + File.separator + "protos")
//        File dirController = new File(rootDir + File.separator + "controller")
//        File dirRoutes = new File(rootDir + File.separator + "routes")
//
//        if (!dirProto.exists()) {
//            dirProto.mkdirs()
//            println "请将proto文件放置在 " + dirProto.path + " 路径下"
//            return
//        } else {
//            def count = 0
//            dirProto.eachFile {
//                count++
//            }
//            if (0 == count) {
//                println "请将proto文件放置在 " + dirProto.path + " 路径下"
//                return
//            }
//        }
//
//        if (!dirController.exists()) {
//            dirController.mkdirs()
//        } else {
//            dirController.eachFile {
//                it.delete()
//            }
//        }
//
//        if (!dirRoutes.exists()) {
//            dirRoutes.mkdirs()
//        } else {
//            dirRoutes.eachFile {
//                it.delete()
//            }
//        }
//
//        File dirMockController = new File(rootDir + File.separator + "mock-data" + File.separator + "controller")
//        File dirMockRoutes = new File(rootDir + File.separator + "mock-data" + File.separator + "routes")
//        if (!dirMockController.exists()) {
//            dirMockController.mkdirs()
//        } else {
//            dirMockController.eachFile {
//                it.delete()
//            }
//        }
//
//        if (!dirMockRoutes.exists()) {
//            dirMockRoutes.mkdirs()
//        } else {
//            dirMockRoutes.eachFile {
//                it.delete()
//            }
//        }
//
//        // ==================== 1
//        generateController(dirProto, dirController)
//        generateRouteFile(dirProto, dirRoutes)
//        generateRouteIndexFile(dirRoutes, dirRoutes)
//
//        parseMessage(dirProto)
//        parseToJson(dirProto, dirRoutes)
//
//        // ==================== 2
//        generateMockController(dirProto, dirMockController)
//        generateMockRouteFile(dirProto, dirMockRoutes)
//        generateMockRouteIndexFile(dirMockRoutes, dirMockRoutes)
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
//                    if (it.contains("//")) {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.lastIndexOf("{")).trim())
//                    } else {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//                    }
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
//                    outFile.append "    " + methodName + " = (req, res, next) => {\n"
//                    outFile.append "        var params = {};\n"
//                    outFile.append "        for (var key in req.body){\n"
//                    outFile.append "            params[key] = req.body[key];\n"
//                    outFile.append "        }\n"
//                    outFile.append "        var metadata = new grpc.Metadata();\n"
//                    outFile.append "        try {\n"
//                    outFile.append "            var grpcHeaderData = JSON.parse(req.headers['x-jh-grpc-header'])||{};\n"
//                    outFile.append "            for (var key in grpcHeaderData) {\n"
//                    outFile.append "                var substr = key.substr(key.length-4,4)||'';\n"
//                    outFile.append "                if (substr === \"-bin\") {\n"
//                    outFile.append "                    metadata.add(key,Buffer.from((grpcHeaderData[key]).toString, 'ascii'));\n"
//                    outFile.append "                } else {\n"
//                    outFile.append "                    metadata.add(key,(grpcHeaderData[key]).toString());\n"
//                    outFile.append "                }\n"
//                    outFile.append "            }\n"
//                    outFile.append "            this.client." + methodName + "(params,metadata, function(err, response) {\n"
//                    outFile.append "                if(err) {\n"
//                    outFile.append "                    res.status(500).send(err);\n"
//                    outFile.append "                } else {\n"
//                    outFile.append "                    res.send(response);\n"
//                    outFile.append "                }\n"
//                    outFile.append "            });\n"
//                    outFile.append "        } catch (e) {\n"
//                    outFile.append "            res.status(500).send(e);\n"
//                    outFile.append "        }\n"
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
//                    if (it.contains("//")) {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.lastIndexOf("{")).trim())
//                    } else {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//                    }
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
//    // ====================
//    static String toLowerCaseFirstC(String s) {
//        if (Character.isLowerCase(s.charAt(0)))
//            return s;
//        else
//            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
//    }
//
//    // ====================
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
//        String describe // annotation
//        String tableHeaderName
//        ArrayList<MessageProp> props
//    }
//
//    // ====================
//    static class ServiceSimple {
//        String serviceName
//        String describe
//        ArrayList<ServiceProp> services
//    }
//
//    static class ServiceProp {
//        String service
//        String path
//        String describe
//        def reqMessage
//        def resMessage
//        int streamFlag // 0普通; 1 request stream; 2 response stream; 3 both stream
//    }
//
//    // ==================== to JSON
//    static void parseMessage(File dirIn) {
//        dirIn.eachFile {
//            if ("proto".equals(it.path.substring(it.path.lastIndexOf(".") + 1))) {
//                def lines = it.readLines("utf-8")
//                CopyOnWriteArrayList list = new CopyOnWriteArrayList(lines)
//                resetCondition()
//                parseSimpleMessage(list)
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
//    static void resetCondition() {
//        flagStart = false
//        flagEnd = false
//        indexStart = 0
//        indexEnd = 0
//        index = 0
//    }
//
//    static void parseSimpleMessage(CopyOnWriteArrayList lines) {
//        String packageName
//
//        lines.each {
//            if (it.trim().startsWith("package")) {
//                packageName = it.substring(8, it.length() - 1)
//            }
//
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
//                parseMessageBlock(lines.subList(indexStart, indexEnd + 1), packageName)
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
//    static void parseMessageBlock(messageLines, packageName) {
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
//                    messageSingle.message = packageName + "." + messageName
//                    //if (messageMap.containsKey(messageName)) {
//                    //    println "有名称相同的Message定义，请修改。MessageName --- " + messageName
//                    //    Thread.currentThread().stop()
//                    //}
//                }
//            } else if (it.trim().startsWith("}")) {
//                messageSingle.props = messageProps
//            } else {
//                if (it.contains(";")) {
//                    def messageProp = new MessageProp()
//
//                    if (it.trim().startsWith("repeated")) {
//                        def propLine = it.split(";")
//
//                        def typeAndName = propLine[0].trim().split("=")[0].trim().split(" ")
//
//                        messageProp.type = "array<" + typeAndName[1].trim() + ">"
//                        messageProp.property = typeAndName[2].trim()
//
//                        if (propLine.size() > 1) {
//                            def desc = propLine[1].trim()
//
//                            if (desc.contains("//")) {
//                                def firstIndex = desc.indexOf("//")
//                                def lastIndex = desc.lastIndexOf("//")
//                                if (firstIndex == lastIndex) {
//                                    messageProp.describe = desc.substring(firstIndex + 2).trim()
//                                    messageProp.tableHeaderName = ""
//                                } else {
//                                    messageProp.describe = desc.substring(firstIndex + 2, lastIndex).trim()
//                                    messageProp.tableHeaderName = desc.substring(lastIndex + 2).trim()
//                                }
//                            } else {
//                                messageProp.describe = ""
//                                messageProp.tableHeaderName = ""
//                            }
//                        } else {
//                            messageProp.describe = ""
//                            messageProp.tableHeaderName = ""
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
//                                def firstIndex = desc.indexOf("//")
//                                def lastIndex = desc.lastIndexOf("//")
//                                if (firstIndex == lastIndex) {
//                                    messageProp.describe = desc.substring(firstIndex + 2).trim()
//                                    messageProp.tableHeaderName = ""
//                                } else {
//                                    messageProp.describe = desc.substring(firstIndex + 2, lastIndex).trim()
//                                    messageProp.tableHeaderName = desc.substring(lastIndex + 2).trim()
//                                }
//                            } else {
//                                messageProp.describe = ""
//                                messageProp.tableHeaderName = ""
//                            }
//                        } else {
//                            messageProp.describe = ""
//                            messageProp.tableHeaderName = ""
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
//                        def thisFileName = it.name
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
//                                // serviceSimple.packageName = packageName
//                            }
//
//                            if (it.startsWith("service")) {
//                                // name
//                                serviceName = toLowerCaseFirstC(it.substring(8)).trim()
//                                if (serviceName.contains("{")) {
//                                    serviceName = serviceName.substring(0, serviceName.indexOf("{")).trim()
//                                }
//                                serviceSimple.serviceName = serviceName
//
//                                // 注释
//                                def indexIt = protoFileLines.indexOf(it)
//                                if (indexIt > 1) {
//                                    def previous = protoFileLines.get(indexIt - 1)
//                                    if (previous.startsWith("//")) {
//                                        serviceSimple.describe = previous.substring(previous.indexOf("//") + 2).trim()
//                                    } else {
//                                        serviceSimple.describe = ""
//                                    }
//                                } else {
//                                    println "proto文件格式错误，请检查 --- " + thisFileName
//                                    println "错误位置 --- " + thisFileName + " --- " + it
//                                    return
//                                }
//                            }
//
//                            if (it.trim().startsWith("rpc")) {
//
//                                if (it.trim().contains("{")) {
//                                    def rpcLine = it.trim().split("\\{")
//                                    def pre = rpcLine[0].trim().split(" ")
//                                    def methodName = toLowerCaseFirstC(pre[1])
//
//                                    def before = rpcLine[0].trim()
//                                    def firstKGIndex = rpcLine[0].trim().indexOf(" ")
//                                    before = before.substring(firstKGIndex + 1).trim()
//                                    def secondKGIndex = before.indexOf(" ")
//                                    before = before.substring(secondKGIndex + 1).trim()
//
//                                    def inAndOut = before.split("returns")
//                                    def inString = inAndOut[0].trim().substring(1, inAndOut[0].trim().length() - 1)
//                                    def outString = inAndOut[1].trim().substring(1, inAndOut[1].trim().length() - 1)
//
//                                    def argIn, argOut
//                                    def streamFlag1, streamFlag2
//                                    if (!inString.contains("stream ")) {
//                                        argIn = inString
//                                        streamFlag1 = 0
//                                    } else {
//                                        argIn = inString.split(" ")[1].trim()
//                                        streamFlag1 = 1
//                                    }
//
//                                    if (!outString.contains("stream ")) {
//                                        argOut = outString
//                                        streamFlag2 = 0
//                                    } else {
//                                        argOut = outString.split(" ")[1].trim()
//                                        streamFlag2 = 1
//                                    }
//
//                                    if (streamFlag1 == 0 && streamFlag2 == 0) {
//                                        serviceProp.streamFlag = 0
//                                    }
//                                    if (streamFlag1 == 1 && streamFlag2 == 0) {
//                                        serviceProp.streamFlag = 1
//                                    }
//                                    if (streamFlag1 == 0 && streamFlag2 == 1) {
//                                        serviceProp.streamFlag = 2
//                                    }
//                                    if (streamFlag1 == 1 && streamFlag2 == 1) {
//                                        serviceProp.streamFlag = 3
//                                    }
//
//                                    if (!argIn.contains(".")) {
//                                        argIn = packageName + "." + argIn
//                                    }
//
//                                    if (!argOut.contains(".")) {
//                                        argOut = packageName + "." + argOut
//                                    }
//
//                                    serviceProp.service = methodName
//                                    serviceProp.path = serviceName + "/" + methodName
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
//                                    // 注释
//                                    def indexIt = protoFileLines.indexOf(it)
//                                    if (indexIt < protoFileLines.size() - 1) {
//                                        def next = protoFileLines.get(indexIt + 1).trim()
//                                        if (next.startsWith("}")) {
//                                            if (next.contains("//")) {
//                                                serviceProp.describe = next.substring(next.indexOf("//") + 2).trim()
//                                            } else {
//                                                serviceProp.describe = ""
//                                            }
//                                        } else {
//                                            println "proto文件格式错误，请检查 --- " + thisFileName
//                                            println "错误位置 --- " + thisFileName + " --- " + it
//                                            return
//                                        }
//                                    } else {
//                                        println "proto文件格式错误，请检查 --- " + thisFileName
//                                        println "错误位置 --- " + thisFileName + " --- " + it
//                                        return
//                                    }
//
//                                    listServiceProp.add(serviceProp)
//                                } else {
//                                    println "格式错误 --- rpc行不符合规则，请检查"
//                                    println "错误位置 --- " + thisFileName + " --- " + it
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
//
//    // ====================
//    static void generateMockController(File dirIn, File dirOut) {
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
//                    outFile.append("import Mock from 'mockjs'\n")
//                    outFile.append("var createError = require('http-errors');\n")
//                    outFile.append("import protoData from '../../routes/json'\n")
//                    outFile.append("import " + packageName + "Mock from '../mock/" + toUpperCaseFirstOne(packageName) + "'\n");
//                    outFile.append("\n")
//                }
//
//                if (it.startsWith("service")) {
//                    if (it.contains("//")) {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.lastIndexOf("{")).trim())
//                    } else {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//                    }
//
//                    outFile.append("class " + toUpperCaseFirstOne(packageName) + "Controller {\n")
//                    outFile.append("    constructor() {\n")
//                    outFile.append("        for (var i = 0; i < protoData.length; i ++) {\n")
//                    outFile.append("            var packageData = protoData[i];\n")
//                    outFile.append("            var serviceList = [];\n")
//                    outFile.append("            if ( '" + packageName + "' === ( packageData['packageName']||'' ) ) {\n")
//                    outFile.append("                serviceList = packageData['services'];\n")
//                    outFile.append("                break;\n")
//                    outFile.append("            }\n")
//                    outFile.append("        }\n")
//                    outFile.append("        this.serviceMap = {};\n")
//                    outFile.append("        for (var i = 0; i < serviceList.length; i ++) {\n")
//                    outFile.append("            var serviceData = serviceList[i];\n")
//                    outFile.append("            this.serviceMap[serviceData['service']] = serviceData;\n")
//                    outFile.append("        }\n")
//                    outFile.append("    }\n")
//                }
//
//                if (it.trim().startsWith("rpc")) {
//                    methodName = toLowerCaseFirstC(it.trim().split(" ")[1].trim())
//                    outFile.append "    " + methodName + " = (req, res, next) => {\n"
//                    outFile.append "        var service = this.serviceMap['" + methodName + "'];\n"
//                    outFile.append "        if (!service) {\n"
//                    outFile.append "            next(createError(500,'service 为空！！！'))\n"
//                    outFile.append "        } else  {\n"
//                    outFile.append "            var resMsg = (service['resMessage']||{})['message'];\n"
//                    outFile.append "            if (!resMsg) {\n"
//                    outFile.append "                next(createError(500,'resMessage 异常！！！'))\n"
//                    outFile.append "            } else {\n"
//                    outFile.append "                var mockData = " + packageName + "Mock[resMsg];\n"
//                    outFile.append "                if (!mockData) {\n"
//                    outFile.append "                    next(createError(500,'mockData 异常！！！'))\n"
//                    outFile.append "                } else {\n"
//                    outFile.append "                    var data = Mock.mock(mockData)\n"
//                    outFile.append "                    res.send(JSON.stringify(data))\n"
//                    outFile.append "                }\n"
//                    outFile.append "            }\n"
//                    outFile.append "        }\n"
//                    outFile.append "    }\n"
//                }
//            }
//            if (null != outFile && outFile.exists()) {
//                outFile.append("};\n")
//                outFile.append("\n")
//                outFile.append "export default new " + toUpperCaseFirstOne(packageName) + "Controller();\n"
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
//    static void generateMockRouteFile(File dirIn, File dirOut) {
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
//                    if (it.contains("//")) {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.lastIndexOf("{")).trim())
//                    } else {
//                        serviceName = toLowerCaseFirstC(it.substring(8, it.length() - 2))
//                    }
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
//    static void generateMockRouteIndexFile(File dirIn, File dirOut) {
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
//        outFile.append("module.exports = app => {\n")
//
//        dirIn.eachFile {
//            if (!(".").equals(it.name.substring(0, 1))) {
//                def fileName = it.name.substring(0, it.name.length() - 3)
//                if (!"index".equals(fileName) || it.name.substring(0, 1).equals(".")) {
//                    outFile.append("    app.use('/test/" + fileName + "'," + fileName + ");\n")
//                }
//            }
//        }
//
//        outFile.append("}\n")
//        outFile.append("\n")
//    }
//}