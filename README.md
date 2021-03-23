# Except Pains

Except Pains是本学期软件工程的项目。

目前团队合作有以下建议：

1. 对于自己模块的详细更新信息和目前工作重心放在README中，供大家长期阅读参考。
2. README的更新和其它代码的更新独立，不要在代码更新的同时更新README。更新README时单独`git add README.md`并写下git日志，举例：`(README) 修改了对xx的说明`。git日志见[第3条]。
3. 打印日志的时候可以不使用System.out.println，似乎使用Log.d("some tag", "some info")会是更好的方案。第一个参数是tag名字，暂定子包名（去掉com.example.ExceptPains这样的公有前缀）.类名.方法名。第二个参数是本次调试的输出信息。
4. 虽然README的更新由git管理，能从git信息中看到编辑内容，但是在网页上看的并不方便，在每个块之后应该用`>>>>>>`表明信息来源。
5. 编辑者最好不用真名吧。

>>>>>> 2020.3.23 15时 Simon Yu编辑


