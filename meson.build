project('cameo', 'cpp',
        version: '3.0.1',
        license: 'GPL-v3',
        default_options: ['default_library=both', 'cpp_std=c++20'],
       )


cppzmq_dep = dependency('cppzmq', required: true, fallback: ['cppzmq'])
zmq_dep = cppzmq_dep
rapidjson_dep = dependency('RapidJSON', required: true)

subdir('cpp/api')
