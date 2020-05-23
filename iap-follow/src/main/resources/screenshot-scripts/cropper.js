var page = require('webpage').create();//创建webpage对象
var sys = require('system');//创建system对象
var address = sys.args[1];//页面加载的地址为参数sys.args[1]
var name = sys.args[2];
if (sys.args.length === 1) {
    console.log('please input like this:phantomjs render.js <some url>');
    phantom.exit();
}else{
    page.open(address, function(status){//页面加载状态为success、fail两种
        if (status !== 'success') {//状态为fail时，控制台打印，载入页面失败，然后退出
            console.log('Fail to load the page!');
            phantom.exit();
        }
        else { //状态为success时，加载页面成功，截图保存为test.png，退出
            var loading = 1;
            page.viewportSize = { width: 3000, height: 3000};
            page.clipRect = {
                left: 0,
                top: 50,
                width: 3000,
                height: 3000
            }
            page.zoomFactor = 3;
            var start = setInterval(function() {
                loading = page.evaluate(function () {//操作页面事件
                    var length = document.getElementsByClassName('loading').length;
                    document.querySelector(".icon-treelike").click(); //用于截图图平台场景探索的时候选中树状图展示
                    if (length === 0) {
                        try {
                            var linkName = document.getElementsByClassName('link-name');
                            var links = document.getElementsByClassName('link');
                            for(var i = 0; i< links.length; i++) {
                                var linksD = links[i].getAttribute("d").split(',');
                                var source = linksD[0].slice(1);
                                var target = linksD[3];
                                if(source > target) {
                                    var testPath = linkName[i].getElementsByTagName('textPath')[0];
                                    var testPathId = testPath.getAttribute('href');
                                    if(!(/\_reverse/.test(testPathId))) {
                                        testPath.setAttribute('href', testPathId.replace('/', '_') + '_reverse')
                                    }
                                }
                            }
                            document.getElementsByClassName('header')[0].style.display = 'none';
                            document.getElementsByClassName('card-wrap')[0].style.display = 'none';
                            document.getElementsByClassName('filter-wrap')[0].style.display = 'none';
                            document.getElementsByClassName('graphtransform-wrap-photo')[0].style.display='none';
                        } catch (e) {
                            length = 0;
                        }
                        return length;
                    }
                    return length;
                });
                if (loading === 0) {
                    setTimeout(function() {
                        clearInterval(start);
                        page.render(name);
                        phantom.exit();
                    }, 2000)
                }
            }, 1000);
        }
    });
    page.onResourceError = function(errorData) {
        console.log('Unable to load resource (URL:' + errorData.url + ')');
        console.log('Error code: ' + errorData.errorCode + '. Description: ' + errorData.errorString);
    };
};