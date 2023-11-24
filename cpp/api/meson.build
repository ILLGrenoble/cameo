project('cameo-api-cpp', 'cpp',
        version: '2.1.1',
        license: 'EUPL-1.2',
        default_options: ['default_library=both', 'cpp_std=c++17']
       )

zmq_dep = dependency('libzmq', required: true)
#thread_dep = dependency('threads', required: true)
#rapidjson_dep = dependency('RapidJSON', required: true)

public_headers_inc = include_directories('include')
subdir('src/base/impl/zmq')
#subdir('src/base/impl')
subdir('src/base')
subdir('src/coms/impl/zmq')
#subdir('src/coms/impl')
subdir('src/coms')
subdir('src/factory')

cameo_api_cpp_lib = library('cameo_api_cpp',
                            sources: [base_impl_zmq_files, base_files,
                                      coms_impl_zmq_files, coms_files,
                                      factory_files],
                            include_directories: [public_headers_inc, base_impl_zmq_inc],
                            dependencies: zmq_dep,
                            install: true,
                           )

cameo_main_header_IN_file = 'include/cameo.h.in'
cameo_main_header_file = configure_file(
  configuration: {
    '@PROJECT_VERSION_MAJOR@': '2',
    '@PROJECT_VERSION_MINOR@': '1',
    '@PROJECT_VERSION_PATCH@': '1'},
  input: cameo_main_header_IN_file,
  output: '@BASENAME@',
  install: true,
  install_dir: get_option('includedir'),
  install_tag: 'devel',
)

install_subdir('include',
               install_dir: get_option('includedir'),
               install_tag: 'devel',
               strip_directory: true,
               exclude_files: cameo_main_header_IN_file,
              )


pkg_mod = import('pkgconfig')
pkg_mod.generate(libraries: cameo_api_cpp_lib,
                 version: '2.1.1',
                 name: 'libcameo-api-cpp',
                 filebase: 'cameo-api-cpp',
                 description: 'Cameo C++ API',
                )
