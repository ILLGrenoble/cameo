#zmq_dep = dependency('libzmq', required: true)
#thread_dep = dependency('threads', required: true)

public_headers_inc = include_directories('include')
# each subdir's meson file provide a XX_files with the list of source files
subdir('src/base/impl/zmq')
subdir('src/base')
subdir('src/coms/impl/zmq')
subdir('src/coms')
subdir('src/factory')

cameo_api_cpp_lib = library('cameo_api_cpp',
                            sources: [base_impl_zmq_files, base_files,
                                      coms_impl_zmq_files, coms_files,
                                      factory_files],
                            include_directories: [public_headers_inc, base_impl_zmq_inc],
                            dependencies: zmq_dep,
                            install: true,
                            version: meson.project_version(),
                            soversion: meson.project_version().split('.')[0],
                            pic: true,
                           )

include_path_subdir = 'cameo'/'api'
cameo_main_header_IN = 'cameo.h.in'
cameo_main_header_IN_file = files('include'/cameo_main_header_IN)
cameo_main_header_file = configure_file(
  configuration: {
    'PROJECT_VERSION_MAJOR': meson.project_version().split('.')[0],
    'PROJECT_VERSION_MINOR': meson.project_version().split('.')[1],
    'PROJECT_VERSION_PATCH': meson.project_version().split('.')[2]},
  input: cameo_main_header_IN_file,
  output: '@BASENAME@',
  install: true,
  install_dir: get_option('includedir')/ include_path_subdir,
  install_tag: 'devel',
)

############################### Headers
install_subdir('include',
               install_dir: get_option('includedir')/ include_path_subdir,
               install_tag: 'devel',
               strip_directory: true,
               exclude_files: cameo_main_header_IN,
              )

############################### Dependencies
generated_headers_inc = include_directories(meson.current_build_dir())
cameo_api_cpp_dep = declare_dependency(
  sources: cameo_main_header_file,
  link_with: cameo_api_cpp_lib,
  include_directories: [public_headers_inc, generated_headers_inc],
  dependencies: rapidjson_dep,
  )

############################### pkgconfig
pkg_mod = import('pkgconfig')
pkg_mod.generate(cameo_api_cpp_lib,
                 version: meson.project_version(),
                 name: 'libcameo-api-cpp',
                 filebase: 'cameo-api-cpp',
                 description: 'Cameo C++ API',
                 subdirs: include_path_subdir,
                )
