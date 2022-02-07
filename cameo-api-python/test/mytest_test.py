#!/usr/bin/python3
import unittest
import sys;




class MainTest(unittest.TestCase):
    def test_a(self):
        # what to test?
        self.assertEqual(cameopython.This, cameopython.This)
        t = cameopython.This
        #print(str(sys.argv[1]))
        t.init('ciao')
        print("\nname=#",t.getName(),"#")

if __name__ == '__main__':
    sys.path.append('/usr/local/src/cameo-api-cpp/cameo-python/build/cameopython-prefix/src/cameopython-build/')
    unittest.main()
