define(function (require) {
    let extend = function (target, methods) {
        if (!target) {
            target = {};
        }
        for (let key in methods) {
            if (methods.hasOwnProperty(key)) {
                target[key] = methods[key];
            }
        }
        return target;
    };
    G = {
        websocket: {},
        events: {},
        request: {},
        cookies: {},
        utils: {},
    };
    extend(G.utils, {
        parse_fix: function (num, sep) {
            if (isNaN(num)) {
                return 0;
            }
            return Math.round(num * Math.pow(10, sep)) / Math.pow(10, sep);
        },
        distance: function (x1, y1, x2, y2, z1, z2) {
            let a = y1 * Math.PI / 180.0 - y2 * Math.PI / 180.0;
            let b = x1 * Math.PI / 180.0 - x2 * Math.PI / 180.0;
            let s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(y1 * Math.PI / 180.0) * Math.cos(y2 * Math.PI / 180.0) * Math.pow(Math.sin(b / 2), 2)));
            s = s * 6378.137;// EARTH_RADIUS;
            s = Math.round(s * 10000) / 10000 * 1000;
            return Math.sqrt(Math.pow(s, 2) + Math.pow((z2 - z1), 2));
        },
        isNum: function (obj) {
            return obj === +obj
        },
        guid: function () {
            /**
             * @return {string}
             */
            function S4() {
                return (((1 + Math.random()) * 0x10000) | 0).toString(16)
                    .substring(1);
            }

            return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-"
                + S4() + S4() + S4());
        },
        base64decode: function (str) {
            return decodeURIComponent(atob(str).split('').map(function (c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
        },
        base64encode: function (str) {
            return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
                function toSolidBytes(match, p1) {
                    return String.fromCharCode('0x' + p1);
                }));
        },
        HashTable: function () {
            // 初始化哈希表的记录条数size
            let size = 0;
            // 创建对象用于接受键值对
            let res = {};
            // 添加关键字，无返回值
            this.add = function (key, value) {
                //判断哈希表中是否存在key，若不存在，则size加1，且赋值
                if (!this.containKey(key)) {
                    size++;
                }
                // 如果之前不存在，赋值； 如果之前存在，覆盖。
                res[key] = value;
            };
            // 删除关键字, 如果哈希表中包含key，并且delete返回true则删除，并使得size减1
            this.remove = function (key) {
                if (this.containKey(key) && (delete res[key])) {
                    size--;
                }
            };
            // 哈希表中是否包含key，返回一个布尔值
            this.containKey = function (key) {
                return (key in res);
            };
            this.match = function (key) {
                for (let prop in res) {
                    if (res.hasOwnProperty(prop)) {
                        if (prop.split('_').indexOf(key) !== -1) {
                            return res[prop];
                        }
                    }
                }
                return null;
            };
            // 哈希表中是否包含value，返回一个布尔值
            this.containValue = function (value) {
                // 遍历对象中的属性值，判断是否和给定value相等
                for (let prop in res) {
                    if (res.hasOwnProperty(prop)) {
                        if (res[prop] === value) {
                            return true;
                        }
                    }
                }
                return false;
            };
            // 根据键获取value,如果不存在就返回null
            this.getValue = function (key) {
                return this.containKey(key) ? res[key] : null;
            };
            // 获取哈希表中的所有value, 返回一个数组
            this.getAllValues = function () {
                let values = [];
                for (let prop in res) {
                    if (res.hasOwnProperty(prop)) {
                        values.push(res[prop]);
                    }
                }
                return values;
            };
            // 根据值获取哈希表中的key，如果不存在就返回null
            this.getKey = function (key) {
                for (let prop in res) {
                    if (prop === key) {
                        return key;
                    }
                }
                // 遍历结束没有return，就返回null
                return null;
            };
            this.updateValue = function (key, value) {
                if (res.hasOwnProperty(key)) {
                    res[key] = value;
                }
            };
            // 获取哈希表中所有的key,返回一个数组
            this.getAllKeys = function () {
                let keys = [];
                for (let prop in res) {
                    if (res.hasOwnProperty(prop)) {
                        keys.push(prop);
                    }
                }
                return keys;
            };
            // 获取哈希表中记录的条数，返回一个数值
            this.getSize = function () {
                return size;
            };
            this.getKV = function (key) {
                if (key in res) {
                    return [key, res[key]]
                }
                return null;
            };
            // 清空哈希表，无返回值
            this.clear = function () {
                size = 0;
                res = {};
            };
            this.getTable = function () {
                return res;
            }
        }
    });
    extend(G.events, {
        async: function (args, handler) {
            setTimeout(function () {
                handler.method.apply(handler.method, [args])
            }, handler.delay * 1000);
        },
        on: function (eventName, listener, id) {
            if (!this.eventMethods) this.eventMethods = [];
            let delay = 0;
            let identifier = "";
            if (arguments.length === 3 && BASE.isNumber(arguments[2])) {
                delay = parseInt(arguments[2]);
                identifier = BASE.guid();
            } else if (arguments.length === 3 && !BASE.isNumber(arguments[2])) {
                identifier = id;
            } else if (arguments.length === 4) {
                delay = parseInt(arguments[3]);
                identifier = id;
            }
            this.eventMethods.push({
                identifier: identifier,
                eventName: eventName,
                method: listener,
                delay: delay
            });
            return identifier;
        }, trigger: function (eventName, data) {
            if (!this.eventMethods) this.eventMethods = [];
            for (let index = 0; index < this.eventMethods.length; index++) {
                let handler = this.eventMethods[index];
                if (handler.eventName === eventName) {
                    this.async(data, handler);
                }
            }
        }, triggerAll: function (eventName, data) {
            if (!this.eventMethods) this.eventMethods = [];
            for (let index = 0; index < this.eventMethods.length; index++) {
                let handler = this.eventMethods[index];
                if (handler.eventName === eventName) {
                    this.async(data, handler);
                }
            }
        }, un: function (eventName, id) {
            for (let index = 0; index < this.eventMethods.length; index++) {
                let handler = this.eventMethods[index];
                if (id) {
                    if (handler.eventName === eventName && handler.identifier === id) {
                        this.eventMethods.splice(index, 1);
                        index--;
                    }
                } else {
                    if (handler.eventName === eventName) {
                        this.eventMethods.splice(index, 1);
                        index--;
                    }
                }
            }
        }, remove: function () {
            this.eventMethods = [];
        }, destroy: function () {
            this.eventMethods = [];
        }
    });
    extend(G.websocket, {
        WS: null,
        clientId: '',
        uri: "",
        connect: function () {
            let thisCallback = this;
            //判断当前浏览器是否支持WebSocket
            if ('WebSocket' in window) {
                this.WS = new WebSocket(this.uri);
            } else {
                console.log('当前浏览器不支持websocket')
            }
            this.WS.onerror = function () {
                console.log("WebSocket连接发生错误");
            };
            this.WS.onopen = function (event) {
                console.log(event);
            };
            this.WS.onmessage = function (event) {
                let data = event.data;
                let msg = JSON.parse(data);
            };
            this.WS.onclose = function () {
                console.log("WebSocket连接关闭");
            };
            window.onbeforeunload = function () {
                thisCallback.close();
            };
        }, send: function (msg, rt) {
            let request = {
                requestType: rt,
                clientId: this.clientId,
                msg: msg
            };
            this.WS.send(JSON.stringify(request));
        },
        close: function () {
            if (this.WS !== null) {
                this.WS.close();
            }
        }
    });
    extend(G.request, {
        base: "http://localhost:8020",
        REQ: function (url, method, data, async) {
            return new Promise((resolve, reject) => {
                $.ajax({
                    type: method,
                    async: async || true,
                    url: this.base + url,
                    data: data || "",
                    contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                    success: res => {
                        resolve(res);
                    },
                    error: err => {
                        reject(err);
                    }
                })
            })
        },
        GET: function (url, data, async) {
            return this.REQ(url, "GET", data, async);
        },
        PUT: function (url, data, async) {
            return this.REQ(url, "PUT", data, async);
        },
        DELETE: function (url, data, async) {
            return this.REQ(url, "DELETE", data, async);
        },
        POST: function (url, data, async) {
            return new Promise((resolve, reject) => {
                $.ajax({
                    type: "POST",
                    async: async || true,
                    url: this.base + url,
                    data: data || "",
                    xhrFields: {
                        'Access-Control-Allow-Origin': '*',
                        'Access-Control-Allow-Methods': 'POST',
                        'Access-Control-Allow-Headers': 'Content-Type'
                    },
                    contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                    success: res => {
                        resolve(res);
                    },
                    error: err => {
                        reject(err);
                    }
                })
            })
        },
        JSONP: function (url, data, async) {
            return new Promise((resolve, reject) => {
                $.ajax({
                    type: "POST",
                    async: async || true,
                    url: this.base + url,
                    data: data || "",
                    dataType: 'jsonp',
                    contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                    success: res => {
                        resolve(res);
                    },
                    error: err => {
                        reject(err);
                    }
                })
            })
        }
    });
    const menu = require('js/menu');
    menu.init();
});