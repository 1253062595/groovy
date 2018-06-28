
import express from 'express';
import helloworld from '../controller/helloworld';

const router = express.Router();

router.post('/sayHello', helloworld.sayHello);
router.post('/sayHello1', helloworld.sayHello1);
router.post('/sayHello2', helloworld.sayHello2);
router.post('/sayHello3', helloworld.sayHello3);
router.post('/sayHello4', helloworld.sayHello4);
router.post('/sayHello5', helloworld.sayHello5);

export default router
