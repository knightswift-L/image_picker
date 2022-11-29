//
//  Generated file. Do not edit.
//

// clang-format off

#include "generated_plugin_registrant.h"

#include <image_picker/image_picker_plugin.h>

void fl_register_plugins(FlPluginRegistry* registry) {
  g_autoptr(FlPluginRegistrar) image_picker_registrar =
      fl_plugin_registry_get_registrar_for_plugin(registry, "ImagePickerPlugin");
  image_picker_plugin_register_with_registrar(image_picker_registrar);
}
