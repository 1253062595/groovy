package protoparse

import groovy.json.JsonGenerator

import java.text.SimpleDateFormat
import java.util.concurrent.CopyOnWriteArrayList

// 解析proto文件，包含stream
class ProtoParser {

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    static Map<String, MessageSimple> messageMap = new HashMap()

    static void main(String[] args) {
        String rootDir = System.getProperty("user.dir")
//        rootDir = rootDir + "\\src\\main"
        File dirProto = new File(rootDir + File.separator + "src" + File.separator + "main" + File.separator + "proto")

        if (!dirProto.exists()) {
            dirProto.mkdirs()
            println "请将proto文件放置在 " + dirProto.path + " 路径下"
            return
        } else {
            def count = 0
            dirProto.eachFile {
                count++
            }
            if (0 == count) {
                println "请将proto文件放置在 " + dirProto.path + " 路径下"
                return
            }
        }

        String pathDest = rootDir + File.separator + "src" + File.separator + "main" + File.separator + "resources"
        File outPath = new File(pathDest)
        if (!outPath.exists()) {
            outPath.mkdirs()
        }

        // ==================== 1
        parseMessage(dirProto)
        parseMessage(dirProto) // 解决auth中message引用base中message问题

        parseToJson(dirProto, outPath)

        // ==================== 2 DTO
        // todo 1
        String temp = rootDir + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "com" + File.separator + "jinhui365" + File.separator + "crm" + File.separator + "appserver"

        File dirOutDto = new File(temp + File.separator + "dto")
        if (!dirOutDto.exists()) {
            dirOutDto.mkdirs()
        }
        // todo 2
        parseToDTO(dirOutDto, "com.jinhui365.crm.appserver.dto.")

        // ==================== 3 Service
        File dirOutService = new File(temp + File.separator + "service")
        if (!dirOutService.exists()) {
            dirOutService.mkdirs()
        }
        // todo 3
        parseToService(dirProto, dirOutService, "package com.jinhui365.crm.appserver.service;")
    }

    // ====================
    static String toLowerCaseFirstC(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    // ====================
    static class MessageSimple {
        String message
        ArrayList<MessageProp> props
    }

    static class MessageProp {
        String property
        String type
        String describe // annotation
        String tableHeaderName
        ArrayList<MessageProp> props
    }

    // ====================
    static class ServiceSimple {
        String serviceName
        String describe
        ArrayList<ServiceProp> services
    }

    static class ServiceProp {
        String service
        String path
        String describe
        def reqMessage
        def resMessage
        int streamFlag // 0普通; 1 request stream; 2 response stream; 3 both stream
    }

    // ==================== to JSON
    static void parseMessage(File dirIn) {
        dirIn.eachFile {
            if ("proto".equals(it.path.substring(it.path.lastIndexOf(".") + 1))) {
                def lines = it.readLines("utf-8")
                CopyOnWriteArrayList list = new CopyOnWriteArrayList(lines)
                resetCondition()
                parseSimpleMessage(list)
            }
        }
    }

    static flagStart = false
    static flagEnd = false
    static indexStart = 0
    static indexEnd = 0
    static index = 0

    static void resetCondition() {
        flagStart = false
        flagEnd = false
        indexStart = 0
        indexEnd = 0
        index = 0
    }

    static void parseSimpleMessage(CopyOnWriteArrayList lines) {
        String packageName
        lines.each {
            if (it.trim().startsWith("package")) {
                packageName = it.substring(8, it.length() - 1)
            }

            if (it.trim().startsWith("message")) {
                indexStart = index
                flagStart = true
            }

            if (it.trim().startsWith("}")) {
                if (flagStart) {
                    indexEnd = index
                    flagEnd = true
                }
            }

            if (flagStart && flagEnd) {

                if (indexStart >= lines.size()) {
                    return
                }

                parseMessageBlock(lines.subList(indexStart, indexEnd + 1), packageName)

                while (indexStart <= indexEnd) {
                    lines.remove(indexStart)
                    indexEnd--
                }

                flagStart = false
                flagEnd = false
                indexStart = 0
                indexEnd = 0
                index = 0

                parseSimpleMessage(lines)
            }

            index++
        }
    }

    static void parseMessageBlock(messageLines, packageName) {
        def messageSingle = new MessageSimple()
        def messageProps = new ArrayList<MessageProp>()

        messageLines.each {
            if (it.trim().startsWith("message")) {
                def messageNameLine = it.trim().split(" ")
                if (messageNameLine.size() >= 2) {
                    def messageName = messageNameLine[1].trim()
                    if ("{".equals(messageName.substring(messageName.length() - 1, messageName.length()))) {
                        messageName = messageName.substring(0, messageName.length() - 1)
                    }
                    messageSingle.message = packageName + "." + messageName
                    //if (messageMap.containsKey(messageName)) {
                    //    println "有名称相同的Message定义，请修改。MessageName --- " + messageName
                    //    Thread.currentThread().stop()
                    //}
                }
            } else if (it.trim().startsWith("}")) {
                messageSingle.props = messageProps
            } else {
                if (it.contains(";") && !it.trim().startsWith("//")) {
                    def messageProp = new MessageProp()

                    if (it.trim().startsWith("repeated")) {
                        def propLine = it.split(";")

                        def typeAndName = propLine[0].trim().split("=")[0].trim().split(" ")

                        messageProp.type = "array<" + typeAndName[1].trim() + ">"
                        messageProp.property = typeAndName[2].trim()

                        if (propLine.size() > 1) {
                            def desc = propLine[1].trim()

                            if (desc.contains("//")) {
                                def firstIndex = desc.indexOf("//")
                                def lastIndex = desc.lastIndexOf("//")
                                if (firstIndex == lastIndex) {
                                    messageProp.describe = desc.substring(firstIndex + 2).trim()
                                    messageProp.tableHeaderName = ""
                                } else {
                                    messageProp.describe = desc.substring(firstIndex + 2, lastIndex).trim()
                                    messageProp.tableHeaderName = desc.substring(lastIndex + 2).trim()
                                }
                            } else {
                                messageProp.describe = ""
                                messageProp.tableHeaderName = ""
                            }
                        } else {
                            messageProp.describe = ""
                            messageProp.tableHeaderName = ""
                        }

                        if (messageMap.containsKey(parseTypeMessage(typeAndName[1].trim(), packageName))) {
                            messageProp.props = messageMap.get(parseTypeMessage(typeAndName[1].trim(), packageName)).props
                        }

                    } else {
                        def propLine = it.split(";")
                        def typeAndName = propLine[0].trim().split("=")[0].trim().split(" ")

                        messageProp.property = typeAndName[1].trim()
                        messageProp.type = typeAndName[0].trim()

                        if (propLine.size() > 1) {
                            def desc = propLine[1].trim()
                            if (desc.contains("//")) {
                                def firstIndex = desc.indexOf("//")
                                def lastIndex = desc.lastIndexOf("//")
                                if (firstIndex == lastIndex) {
                                    messageProp.describe = desc.substring(firstIndex + 2).trim()
                                    messageProp.tableHeaderName = ""
                                } else {
                                    messageProp.describe = desc.substring(firstIndex + 2, lastIndex).trim()
                                    messageProp.tableHeaderName = desc.substring(lastIndex + 2).trim()
                                }
                            } else {
                                messageProp.describe = ""
                                messageProp.tableHeaderName = ""
                            }
                        } else {
                            messageProp.describe = ""
                            messageProp.tableHeaderName = ""
                        }

//                        if (messageMap.containsKey(messageProp.type)) {
//                            messageProp.props = messageMap.get(messageProp.type).props
//                        }
                        if (messageMap.containsKey(parseTypeMessage(messageProp.type, packageName))) {
                            messageProp.props = messageMap.get(parseTypeMessage(messageProp.type, packageName)).props
                        }
                    }

                    messageProps.add(messageProp)
                }
            }
        }

        if (null != messageSingle.message && null != messageSingle.props) {
            messageMap.put(messageSingle.message, messageSingle)
        }
    }

    static void parseToJson(File dirIn, File dirOut) {
        ArrayList<ServiceSimple> listServiceSimple = new ArrayList<>()

        ServiceSimple serviceSimple
        ArrayList<ServiceProp> listServiceProp

        dirIn.eachFile {
            if (it.path.contains(".")) {
                def fullName = it.path.split("\\.")
                if (fullName.size() > 1) {
                    if ("proto".equals(fullName[1].trim())) {

                        def thisFileName = it.name
                        def protoFileLines = it.readLines("utf-8")

                        // deal with package and services
                        def packageName
                        def serviceName
                        serviceSimple = new ServiceSimple()
                        listServiceProp = new ArrayList<>()
                        ServiceProp serviceProp

                        protoFileLines.each {
                            serviceProp = new ServiceProp()

                            if (it.trim().startsWith("package")) {
                                packageName = it.substring(8, it.length() - 1)
                            }

                            if (it.startsWith("service")) {
                                // name
                                serviceName = toLowerCaseFirstC(it.substring(8)).trim()
                                if (serviceName.contains("{")) {
                                    serviceName = serviceName.substring(0, serviceName.indexOf("{")).trim()
                                }
                                serviceSimple.serviceName = serviceName

                                // 注释
                                def indexIt = protoFileLines.indexOf(it)
                                if (indexIt > 1) {
                                    def previous = protoFileLines.get(indexIt - 1)
                                    if (previous.startsWith("//")) {
                                        serviceSimple.describe = previous.substring(previous.indexOf("//") + 2).trim()
                                    } else {
                                        serviceSimple.describe = ""
                                    }
                                } else {
                                    println "proto文件格式错误，请检查 --- " + thisFileName
                                    println "错误位置 --- " + thisFileName + " --- " + it
                                    return
                                }
                            }

                            if (it.trim().startsWith("rpc")) {

                                if (it.trim().contains("{")) {
                                    def rpcLine = it.trim().split("\\{")
                                    def pre = rpcLine[0].trim().split(" ")
                                    def methodName = toLowerCaseFirstC(pre[1])

                                    def before = rpcLine[0].trim()
                                    def firstKGIndex = rpcLine[0].trim().indexOf(" ")
                                    before = before.substring(firstKGIndex + 1).trim()
                                    def secondKGIndex = before.indexOf(" ")
                                    before = before.substring(secondKGIndex + 1).trim()

                                    def inAndOut = before.split("returns")
                                    def inString = inAndOut[0].trim().substring(1, inAndOut[0].trim().length() - 1)
                                    def outString = inAndOut[1].trim().substring(1, inAndOut[1].trim().length() - 1)

                                    def argIn, argOut
                                    def streamFlag1, streamFlag2
                                    if (!inString.contains("stream ")) {
                                        argIn = inString
                                        streamFlag1 = 0
                                    } else {
                                        argIn = inString.split(" ")[1].trim()
                                        streamFlag1 = 1
                                    }

                                    if (!outString.contains("stream ")) {
                                        argOut = outString
                                        streamFlag2 = 0
                                    } else {
                                        argOut = outString.split(" ")[1].trim()
                                        streamFlag2 = 1
                                    }

                                    if (streamFlag1 == 0 && streamFlag2 == 0) {
                                        serviceProp.streamFlag = 0
                                    }
                                    if (streamFlag1 == 1 && streamFlag2 == 0) {
                                        serviceProp.streamFlag = 1
                                    }
                                    if (streamFlag1 == 0 && streamFlag2 == 1) {
                                        serviceProp.streamFlag = 2
                                    }
                                    if (streamFlag1 == 1 && streamFlag2 == 1) {
                                        serviceProp.streamFlag = 3
                                    }

                                    if (!argIn.contains(".")) {
                                        argIn = packageName + "." + argIn
                                    }

                                    if (!argOut.contains(".")) {
                                        argOut = packageName + "." + argOut
                                    }

                                    serviceProp.service = methodName
                                    serviceProp.path = serviceName + "/" + methodName
                                    if (messageMap.containsKey(argIn)) {
                                        serviceProp.reqMessage = messageMap.get(argIn)
                                    } else {
                                        serviceProp.reqMessage = argIn
                                    }
                                    if (messageMap.containsKey(argOut)) {
                                        serviceProp.resMessage = messageMap.get(argOut)
                                    } else {
                                        serviceProp.resMessage = argOut
                                    }

                                    // 注释
                                    def indexIt = protoFileLines.indexOf(it)
                                    if (indexIt < protoFileLines.size() - 1) {
                                        def next = protoFileLines.get(indexIt + 1).trim()
                                        if (next.startsWith("}")) {
                                            if (next.contains("//")) {
                                                serviceProp.describe = next.substring(next.indexOf("//") + 2).trim()
                                            } else {
                                                serviceProp.describe = ""
                                            }
                                        } else {
                                            println "proto文件格式错误，请检查 --- " + thisFileName
                                            println "错误位置 --- " + thisFileName + " --- " + it
                                            return
                                        }
                                    } else {
                                        println "proto文件格式错误，请检查 --- " + thisFileName
                                        println "错误位置 --- " + thisFileName + " --- " + it
                                        return
                                    }

                                    listServiceProp.add(serviceProp)
                                } else {
                                    println "格式错误 --- rpc行不符合规则，请检查"
                                    println "错误位置 --- " + thisFileName + " --- " + it
                                    return
                                }
                            }
                        }

                        if (listServiceProp.size() > 0) {
                            serviceSimple.services = listServiceProp
                            listServiceSimple.add(serviceSimple)
                        }
                    }
                }
            }
        }

        def fileOut = new File(dirOut, "services.js")
        if (fileOut.exists()) {
            fileOut.delete()
        }

        def generator = new JsonGenerator.Options().excludeNulls().build()

        fileOut.append("var services = ")
        fileOut.append(generator.toJson(listServiceSimple))
        fileOut.append(";")
        fileOut.append("\n")
    }

    // ==================== to DTO
    static void parseToDTO(File dirOut, String packagePath) {
        for (String messageName : messageMap.keySet()) {
            String packageName = messageName.split("\\.")[0].trim()
            String dtoName = messageName.split("\\.")[1].trim()

            File dirPackage = new File(dirOut, packageName)
            if (!dirPackage.exists()) { // 增量更新
                dirPackage.mkdirs()
            }

            File dto = new File(dirPackage, dtoName + "Dto.java")
            PrintWriter printWriter

            if (!dto.exists()) { // 增量更新
                def containsArray = false

                printWriter = dto.newPrintWriter("utf-8")
                printWriter.append("package " + packagePath + packageName + ";")
                printWriter.append("\n\n")

                List<MessageProp> props = messageMap.get(messageName).props
                List<String> messageArray = new ArrayList<>() // 避免重复import

                // import
                props.each {
                    def typeOriginal = it.type
                    if (typeOriginal.startsWith("array<")) {
                        containsArray = true
                        if (typeOriginal.contains(".")) {
                            String temp = typeOriginal.substring(typeOriginal.indexOf("<") + 1, typeOriginal.indexOf(">"))
                            if (null != messageArray && !messageArray.contains(temp)) {
                                printWriter.append("import " + packagePath + temp + "Dto;\n")
                                messageArray.add(temp)
                            }
                        }
                    } else {
                        if (typeOriginal.contains(".")) {
                            if (null != messageArray && !messageArray.contains(typeOriginal)) {
                                printWriter.append("import " + packagePath + typeOriginal + "Dto;\n")
                                messageArray.add(typeOriginal)
                            }
                        }
                    }
                }
                if (containsArray) {
                    printWriter.append("import java.util.List;")
                    printWriter.append("\n\n")
                    containsArray = false
                }

                printWriter.append("public class " + dtoName + "Dto {\n")

                // attrs
                props.each {
                    printWriter.append("\n")

                    def attrType = it.type
                    def attrName = it.property
                    def attrDesc = it.describe
                    def attrTHName = it.tableHeaderName
                    attrType = parseType(attrType)
                    attrName = parseAttrName(attrName)

                    printWriter.append("    private " + attrType + " " + attrName + ";")
                    if (!attrDesc.isEmpty()) {
                        printWriter.append(" // " + attrDesc)
                    }
                    if (!attrTHName.isEmpty()) {
                        printWriter.append(" // " + attrTHName)
                    }
                }

                // getters and setters
                props.each {
                    printWriter.append("\n")

                    def attrType = it.type
                    def attrName = it.property
                    attrType = parseType(attrType)
                    attrName = parseAttrName(attrName)

                    printWriter.append("\n")
                    printWriter.append("    public " + attrType + " get" + toUpperCaseFirstOne(attrName) + "() {\n")
                    printWriter.append("        return " + attrName + ";")
                    printWriter.append("\n")
                    printWriter.append("    }")

                    printWriter.append("\n")
                    printWriter.append("    public void set" + toUpperCaseFirstOne(attrName) + "(" + attrType + " " + attrName + ") {\n")
                    printWriter.append("        this." + attrName + " = " + attrName + ";")
                    printWriter.append("\n")
                    printWriter.append("    }")
                }

                printWriter.append("\n\n")
                printWriter.append("}")

                printWriter.flush()
                printWriter.close()
            }
        }
    }

    static String parseType(String typeOriginal) {
        // 1 先处理list --- list<A.B> list<C> list<string>...
        if (typeOriginal.startsWith("array<")) {
            if (typeOriginal.contains(".")) {
                typeOriginal = "List<" + typeOriginal.substring(typeOriginal.indexOf(".") + 1, typeOriginal.indexOf(">")) + "Dto>"
            } else {
                def temp = typeOriginal.substring(typeOriginal.indexOf("<") + 1, typeOriginal.indexOf(">"))
                temp = parseType2(temp)
                typeOriginal = "List<" + temp + ">"
            }
        } else {
            // 2 再处理非list
            typeOriginal = parseType2(typeOriginal)
        }
        return typeOriginal
    }

    // todo 后续可能添加
    static String parseType2(String typeOriginal) {
        if ("int32".equals(typeOriginal)) {
            typeOriginal = "int"
        } else if ("int64".equals(typeOriginal)) {
            typeOriginal = "long"
        } else if ("string".equals(typeOriginal)) {
            typeOriginal = "String"
        } else if ("bytes".equals(typeOriginal)) {
            typeOriginal = "ByteString"
        } else {
            if (typeOriginal.contains(".")) {
                typeOriginal = typeOriginal.substring(typeOriginal.lastIndexOf(".") + 1) + "Dto"
            } else {
                typeOriginal = typeOriginal + "Dto"
            }
        }
        return typeOriginal
    }

    static String parseAttrName(String attrName) {
        if (null != attrName && attrName.contains("_")) {
            String[] array = attrName.split("\\_")
            for (int i = 1; i < array.length; i++) {
                array[i] = toUpperCaseFirstOne(array[i])
            }

            StringBuilder builder = new StringBuilder();
            for (String s : array) {
                builder.append(s)
            }
            attrName = builder.toString()
        }
        return attrName
    }

    // ==================== to Service
    static void parseToService(File dirIn, File dirOut, String packagePath) {
        dirIn.eachFile {
            if ("proto".equals(it.name.substring(it.name.lastIndexOf(".") + 1))) {
                parseEachFile(it, dirOut, packagePath)
            }
        }
    }

    static void parseEachFile(File protoFile, File dirOut, String packagePath) {
        def packageName
        def javaPackage // option java_package = "com.jinhui365.crm.appserver.grpc.proto";
        List<String> messageArray = new ArrayList<>()

        def serviceName
        def serviceDesc
        def methodName
        def methodDesc

        File fileOut
        PrintWriter printWriter
        boolean newFile = false

        def lines = protoFile.readLines("utf-8")

        // package and top import
        lines.each {
            if (it.trim().startsWith("option java_package =")) {
                def str = it.split("\\=")[1].trim()
                javaPackage = str.substring(1, str.length() - 2)
            }

            if (it.startsWith("service")) {
                // name
                serviceName = toLowerCaseFirstC(it.substring(8)).trim()
                if (serviceName.contains("{")) {
                    serviceName = serviceName.substring(0, serviceName.indexOf("{")).trim()
                }
                serviceName = serviceName.substring(0, serviceName.length() - 3)

                fileOut = new File(dirOut, toUpperCaseFirstOne(serviceName) + "Service.java")
                if (!fileOut.exists()) {
                    printWriter = fileOut.newPrintWriter("utf-8")
                    newFile = true
                    printWriter.append(packagePath + "\n\n")
                    printWriter.append("import io.grpc.stub.StreamObserver;\n")
                    printWriter.append("import org.lognet.springboot.grpc.GRpcService;\n")
                    printWriter.append("import org.slf4j.Logger;\n")
                    printWriter.append("import org.slf4j.LoggerFactory;\n")
                    printWriter.append("import org.springframework.transaction.annotation.Transactional;\n")
                    printWriter.append("import com.jinhui365.crm.appserver.grpc.proto." + toUpperCaseFirstOne(serviceName) + "RpcGrpc;\n\n")

                    if (null != javaPackage) {
                        printWriter.append("import static " + javaPackage + "." + toUpperCaseFirstOne(serviceName) + "Proto.*;\n")
                    }
                }
            }
        }

        // message import
        lines.each {
            if (it.trim().startsWith("package")) {
                packageName = it.substring(8, it.length() - 1)
            }

            if (it.trim().startsWith("option java_package =")) {
                def str = it.split("\\=")[1].trim()
                javaPackage = str.substring(1, str.length() - 2)
            }

            if (it.trim().startsWith("rpc")) {
                if (it.trim().contains("{")) {
                    def rpcLine = it.trim().split("\\{")
                    def before = rpcLine[0].trim()
                    def firstKGIndex = rpcLine[0].trim().indexOf(" ")
                    before = before.substring(firstKGIndex + 1).trim()
                    def secondKGIndex = before.indexOf(" ")
                    before = before.substring(secondKGIndex + 1).trim()

                    def inAndOut = before.split("returns")
                    def inString = inAndOut[0].trim().substring(1, inAndOut[0].trim().length() - 1)
                    def outString = inAndOut[1].trim().substring(1, inAndOut[1].trim().length() - 1)

                    def argIn, argOut
                    if (!inString.contains("stream ")) {
                        argIn = inString
                    } else {
                        argIn = inString.split(" ")[1].trim()
                    }
                    if (!outString.contains("stream ")) {
                        argOut = outString
                    } else {
                        argOut = outString.split(" ")[1].trim()
                    }

                    if (argIn.contains(".")) {
                        String[] array = argIn.split("\\.")
                        if (!packageName.equals(array[0].trim())) {
                            if (!messageArray.contains(argIn)) {
                                if (newFile) {
                                    printWriter.append("import static " + javaPackage + "." + toUpperCaseFirstOne(array[0].trim()) + "Proto." + array[1].trim() + ";\n")
                                }
                                messageArray.add(argIn)
                            }
                        }
                    }
                    if (argOut.contains(".")) {
                        String[] array = argOut.split("\\.")
                        if (!packageName.equals(array[0].trim())) {
                            if (!messageArray.contains(argOut)) {
                                if (newFile) {
                                    printWriter.append("import static " + javaPackage + "." + toUpperCaseFirstOne(array[0].trim()) + "Proto." + array[1].trim() + ";\n")
                                }
                                messageArray.add(argOut)
                            }
                        }
                    }
                } else {
                    println "格式错误 --- rpc行不符合规则，请检查"
                    println "错误位置 --- " + protoFile.name + " --- " + it
                    return
                }
            }
        }

        // service and method
        lines.each {
            if (it.trim().startsWith("package")) {
                packageName = it.substring(8, it.length() - 1)
            }

            if (it.trim().startsWith("option java_package =")) {
                def str = it.split("\\=")[1].trim()
                javaPackage = str.substring(1, str.length() - 2)
            }

            // service
            if (it.startsWith("service")) {
                // name
                serviceName = toLowerCaseFirstC(it.substring(8)).trim()
                if (serviceName.contains("{")) {
                    serviceName = serviceName.substring(0, serviceName.indexOf("{")).trim()
                }
                serviceName = serviceName.substring(0, serviceName.length() - 3)
                if (newFile) {
                    printWriter.append("\n\n")
                }

                // 注释
                def indexIt = lines.indexOf(it)
                if (indexIt > 1) {
                    def previous = lines.get(indexIt - 1)
                    if (previous.startsWith("//")) {
                        serviceDesc = previous.substring(previous.indexOf("//") + 2).trim()
                    } else {
                        serviceDesc = ""
                    }
                } else {
                    println "proto文件格式错误，请检查 --- " + protoFile.name
                    println "错误位置 --- " + protoFile.name + " --- " + it
                    return
                }

                if (newFile) {
                    printWriter.append("/**\n")
                    printWriter.append(" * Date:" + simpleDateFormat.format(new Date()) + "\n")
                    printWriter.append(" * Description:" + (null == methodDesc ? "" : methodDesc) + "\n")
                    printWriter.append(" */\n\n")

                    printWriter.append("@GRpcService\n")
                    printWriter.append("@Transactional\n")
                    printWriter.append("public class " + toUpperCaseFirstOne(serviceName) + "Service extends " + toUpperCaseFirstOne(serviceName) + "RpcGrpc." + toUpperCaseFirstOne(serviceName) + "RpcImplBase {\n\n")
                    printWriter.append("    private static final Logger logger = LoggerFactory.getLogger(" + toUpperCaseFirstOne(serviceName) + "Service.class);\n\n")
                }
            }

            // method
            if (it.trim().startsWith("rpc")) {
                if (it.trim().contains("{")) {
                    def rpcLine = it.trim().split("\\{")
                    def pre = rpcLine[0].trim().split(" ")
                    methodName = toLowerCaseFirstC(pre[1])

                    def before = rpcLine[0].trim()
                    def firstKGIndex = rpcLine[0].trim().indexOf(" ")
                    before = before.substring(firstKGIndex + 1).trim()
                    def secondKGIndex = before.indexOf(" ")
                    before = before.substring(secondKGIndex + 1).trim()

                    def inAndOut = before.split("returns")
                    def inString = inAndOut[0].trim().substring(1, inAndOut[0].trim().length() - 1)
                    def outString = inAndOut[1].trim().substring(1, inAndOut[1].trim().length() - 1)

                    def argIn, argOut
                    def streamFlag1, streamFlag2
                    if (!inString.contains("stream ")) {
                        argIn = inString
                        streamFlag1 = 0
                    } else {
                        argIn = inString.split(" ")[1].trim()
                        streamFlag1 = 1
                    }

                    if (!outString.contains("stream ")) {
                        argOut = outString
                        streamFlag2 = 0
                    } else {
                        argOut = outString.split(" ")[1].trim()
                        streamFlag2 = 1
                    }

                    // 注释
                    def indexIt = lines.indexOf(it)
                    if (indexIt < lines.size() - 1) {
                        def next = lines.get(indexIt + 1).trim()
                        if (next.startsWith("}")) {
                            if (next.contains("//")) {
                                methodDesc = next.substring(next.indexOf("//") + 2).trim()
                            } else {
                                methodDesc = ""
                            }
                        } else {
                            println "proto文件格式错误，请检查 --- " + protoFile.name
                            println "错误位置 --- " + protoFile.name + " --- " + it
                            return
                        }
                    } else {
                        println "proto文件格式错误，请检查 --- " + protoFile.name
                        println "错误位置 --- " + protoFile.name + " --- " + it
                        return
                    }

                    if (newFile) {
                        printWriter.append("    /**\n")
                        printWriter.append("     * " + (null == methodDesc ? "" : methodDesc) + "\n")
                        printWriter.append("     *\n")
                        printWriter.append("     * @param request\n")
                        printWriter.append("     * @param responseObserver\n")
                        printWriter.append("     */\n")
                        printWriter.append("    @Override\n")
                        printWriter.append("    public void " + methodName +
                                "(" + toUpperCaseFirstOne(argIn.contains(".") ? argIn.substring(argIn.indexOf(".") + 1) : argIn)
                                + " request, StreamObserver<" + toUpperCaseFirstOne(argOut.contains(".") ? argOut.substring(argOut.indexOf(".") + 1) : argOut)
                                + "> responseObserver) {\n\n"
                        )
                        printWriter.append("    }\n\n")
                    }

                } else {
                    println "格式错误 --- rpc行不符合规则，请检查"
                    println "错误位置 --- " + protoFile.name + " --- " + it
                    return
                }
            }
        }

        if (null != fileOut && newFile) {
            printWriter.append("}\n\n")
            printWriter.flush()
            printWriter.close()
        }
    }

    // ==================== new add
    static String parseTypeMessage(String typeOriginal, String packageName) {
        if (typeOriginal.contains(".")) {
            return typeOriginal
        } else {
            String temp = packageName + "." + typeOriginal
            for (String key : messageMap.keySet()) {
                if (temp.equals(key)) {
                    return temp
                }
            }
        }
        return typeOriginal
    }
}