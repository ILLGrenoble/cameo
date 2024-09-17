project('cameo', 'cpp',
        version: '2.1.1',
        license: 'EUPL-1.2',
        default_options: ['default_library=both', 'cpp_std=c++20'],
       )


cppzmq_dep = dependency('cppzmq', required: true, fallback: ['cppzmq'])
zmq_dep = cppzmq_dep
rapidjson_dep = dependency('RapidJSON', required: true)

subdir('cpp/api')
