```

# checkout the cameo package
git clone git@code.ill.fr:cameo/cameo.git

# add the dedicated repository for the C++ API
git remote add cpp git@code.ill.fr:cameo/cameo-api-cpp.git
git fetch cpp

# make a branch with only the commits of the C++ API from the main CAMEO repo
# the new commits w.r.t. the previous split are annotate with "(split) " 
# prepended to the comment
git subtree split -P cameo-api-cpp --annotate='(split) ' -b main_master

# make a branch in local that tracks the master branch of the dedicated C++ API repo
git branch --track cpp_master cpp/old_master


git checkout main_master

# to see on the command line the history:
git log --graph main_master cpp_master

# in order to preserve the history in the main CAMEO, 
# we have to rewrite the history in the C++ API branch and rebase w.r.t. the main
git rebase -i main_master cpp_master

git checkout master
git subtree merge -P cameo-api-cpp/ cpp_master
# you need to solve few conflicts :-)

# here's how to visualize the history in a clean way:
git log --graph --grep='(split)' --invert-grep master 
```

This way the history is present on both sides.

-----------------------------------------
# Example: propagation of changes from MAIN/master to CPP/devel
```
git clone git@code.ill.fr:cameo/cameo.git
git remote add cpp git@code.ill.fr:cameo/cameo-api-cpp.git
git fetch cpp

git subtree split -P cameo-api-cpp --annotate='(split3) ' --rejoin -b main_master
git branch --track cpp/devel cpp_devel

git checkout cpp_devel
git merge main_master
git push cpp cpp_devel
git checkout master
git push origin 
```

