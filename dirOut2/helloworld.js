
import grpc from 'grpc';

let PROTO_PATH = __dirname + '/../protos/helloworld.proto';
let proto = grpc.load(PROTO_PATH).helloworld;
let client =  new proto.Greeter('localhost:50051', grpc.credentials.createInsecure());
class HelloworldController {
    constructor() {
    }
    async sayHello (req, res, next) {
        var params = {};
        for (var key in req.body){
            params[key] = req.body[key];
        }
        client.sayHello(params, function(err, response) {
            if(err) {
                next(err);
            } else {
                res.send(response);
            }
        });
    }
    async sayHello1 (req, res, next) {
        var params = {};
        for (var key in req.body){
            params[key] = req.body[key];
        }
        client.sayHello1(params, function(err, response) {
            if(err) {
                next(err);
            } else {
                res.send(response);
            }
        });
    }
    async sayHello2 (req, res, next) {
        var params = {};
        for (var key in req.body){
            params[key] = req.body[key];
        }
        client.sayHello2(params, function(err, response) {
            if(err) {
                next(err);
            } else {
                res.send(response);
            }
        });
    }
    async sayHello3 (req, res, next) {
        var params = {};
        for (var key in req.body){
            params[key] = req.body[key];
        }
        client.sayHello3(params, function(err, response) {
            if(err) {
                next(err);
            } else {
                res.send(response);
            }
        });
    }
    async sayHello4 (req, res, next) {
        var params = {};
        for (var key in req.body){
            params[key] = req.body[key];
        }
        client.sayHello4(params, function(err, response) {
            if(err) {
                next(err);
            } else {
                res.send(response);
            }
        });
    }
    async sayHello5 (req, res, next) {
        var params = {};
        for (var key in req.body){
            params[key] = req.body[key];
        }
        client.sayHello5(params, function(err, response) {
            if(err) {
                next(err);
            } else {
                res.send(response);
            }
        });
    }
};

export default new HelloworldController();
