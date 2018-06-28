
'use strict';

import helloworld from './helloworld';

export default app => {
    app.use('/helloworld',helloworld);
}

