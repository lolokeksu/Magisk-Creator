package com.magisk.next.viewmodel

import com.magisk.next.model.ModuleFile

class TemplateEngine(private val data: ModuleData) {

    fun applyTemplate(template: ModuleTemplate) {
        when (template) {
            ModuleTemplate.EMPTY -> resetToTemplate()
            ModuleTemplate.DEBLOATER -> applyDebloaterTemplate()
            ModuleTemplate.HOSTS_BLOCKER -> applyHostsBlockerTemplate()
            ModuleTemplate.KERNEL_TWEAKS -> applyKernelTweaksTemplate()
        }
    }

    fun resetToTemplate() {
        data.moduleId = "template_module"
        data.moduleName = "Template Module"
        data.moduleVersion = "1.0.0"
        data.moduleVersionCode = "1"
        data.moduleAuthor = "Developer"
        data.moduleDescription = "Шаблонный модуль Magisk"
        data.moduleLink = "https://github.com/"
        data.moduleChangelog = "Версия 1.0.0\n- Изначальный релиз шаблона"
        data.customizeScript = "#!/system/bin/sh\nui_print \"Installing template...\"\nui_print \"Done\""
        data.serviceScript = "#!/system/bin/sh\nwhile [ \"\$(getprop sys.boot_completed)\" != \"1\" ]; do\n  sleep 1\ndone\n# Your service code here\nexit 0"
        data.postFsScript = "#!/system/bin/sh\n# This script runs after /data is mounted\nexit 0"
    }

    private fun applyDebloaterTemplate() {
        data.moduleId = "debloater"
        data.moduleName = "System Debloater"
        data.moduleVersion = "1.0.0"
        data.moduleVersionCode = "1"
        data.moduleAuthor = ""
        data.moduleDescription = "Removes unwanted system apps"
        data.customizeScript = """#!/system/bin/sh
# Debloat script
# Remove unwanted system apps

APPS_TO_REMOVE="
system/app/YouTube
system/app/Maps
system/app/Drive
"

for app in ${'$'}APPS_TO_REMOVE; do
  if [ -d "${'$'}app" ]; then
    ui_print "Removing ${'$'}app"
    rm -rf "${'$'}app"
  fi
done

ui_print "Debloat complete!"
"""
        data.serviceScript = ""
        data.postFsScript = ""
        data.moduleFiles.clear()
    }

    private fun applyHostsBlockerTemplate() {
        data.moduleId = "hosts_blocker"
        data.moduleName = "Hosts Blocker"
        data.moduleVersion = "1.0.0"
        data.moduleVersionCode = "1"
        data.moduleAuthor = ""
        data.moduleDescription = "Blocks ads and trackers via hosts file"
        data.customizeScript = """#!/system/bin/sh
# Hosts blocker
# Replaces /system/etc/hosts with a custom one

ui_print "Installing custom hosts file..."
cp -f ${'$'}MODPATH/system/etc/hosts /system/etc/hosts
chmod 644 /system/etc/hosts
ui_print "Hosts file installed!"
"""
        data.serviceScript = ""
        data.postFsScript = ""
        data.moduleFiles.clear()
        data.moduleFiles.add(
            ModuleFile(
                name = "system/etc/hosts",
                content = "127.0.0.1 localhost\n# Add your blocked domains here",
                permissions = "0644",
                type = "config"
            )
        )
    }

    private fun applyKernelTweaksTemplate() {
        data.moduleId = "kernel_tweaks"
        data.moduleName = "Kernel Tweaks"
        data.moduleVersion = "1.0.0"
        data.moduleVersionCode = "1"
        data.moduleAuthor = ""
        data.moduleDescription = "Applies kernel parameter tweaks"
        data.customizeScript = """#!/system/bin/sh
# Kernel tweaks
# Adjust kernel parameters

# Example: set I/O scheduler
echo "deadline" > /sys/block/mmcblk0/queue/scheduler

# Example: increase read ahead buffer
echo "2048" > /sys/block/mmcblk0/queue/read_ahead_kb

ui_print "Kernel tweaks applied!"
"""
        data.serviceScript = """#!/system/bin/sh
# Re-apply tweaks after boot
while [ "\$(getprop sys.boot_completed)" != "1" ]; do
  sleep 1
done

echo "deadline" > /sys/block/mmcblk0/queue/scheduler
echo "2048" > /sys/block/mmcblk0/queue/read_ahead_kb
exit 0
"""
        data.postFsScript = ""
        data.moduleFiles.clear()
    }
}