import sys
import numpy as np
import pymetis


def partition(adj_list, cuts: int):
    partitions, mem = pymetis.part_graph(cuts, adjacency=adj_list)
    return [partitions, mem]


if __name__ == '__main__':
    s = sys.argv[1]
    prop_cuts = int(sys.argv[2])
    s = list(map(str, s.split('.')))
    prop = []
    for x in s:
        x = x[1:]
        x = x[:len(x)-1]
        x = list(map(int, x.split(',')))
        prop.append(x)
    n_cuts, membership = partition(prop, prop_cuts)
    print(n_cuts)
    print(membership)
