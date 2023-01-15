# Except Pains Bookkeeping

安卓平台账单记录应用。

## 1月的改动

由于使用的依赖 **androidx.core:core:1.10.0-alpha01** 允许的最低SDK Version为33，因而将构建SDK版本修改了。

## 问题

### Kotlin 版本不对

更新Android Studio的Kotlin插件为最新，并将build.gradle中的buildscript { ext.kotlin_version= } 修改成相同的版本。
