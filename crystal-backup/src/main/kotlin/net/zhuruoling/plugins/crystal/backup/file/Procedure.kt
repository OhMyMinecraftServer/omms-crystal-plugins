package net.zhuruoling.plugins.crystal.backup.file

abstract class Procedure(val scheduledTimeMillis: Long,private val name: String) : Thread(name)