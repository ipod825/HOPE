import os
from config import thirdparty_dir
from utils import bash
import config

libdai_dir = os.path.join(thirdparty_dir, 'libdai')
uai2fg = os.path.join(libdai_dir, 'uai2fg')
jt_solve = os.path.join(libdai_dir, 'jt_solve')
mf_solve = os.path.join(libdai_dir, 'mf_solve')
trwbp_solve = os.path.join(libdai_dir, 'trwbp_solve')


def uai2dai(path):
    bash(['cp', path+'*', config.tmp_dir])
    ori_path = os.path.basename(path)
    path = os.path.join(config.tmp_dir, ori_path)
    new_path = '.'.join(ori_path.split('.')[:-1])
    new_path = os.path.join(config.tmp_dir, new_path)

    bash([uai2fg, new_path, '1', '0'])
    return new_path+'.0.fg'


class JunctionTree(object):
    def solve(self, path):
        problem = uai2dai(path)
        res = bash([jt_solve, problem])[0]
        return float(res)


class MF(object):
    def solve(self, path):
        problem = uai2dai(path)
        res = bash([mf_solve, problem])[0]
        return float(res)


class TRWBP(object):
    def solve(self, path):
        problem = uai2dai(path)
        res = bash([trwbp_solve, problem])[0]
        return float(res)
