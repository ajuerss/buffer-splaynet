import community as community_louvain
import networkx as nx
import numpy as np
import json
import collections


if __name__ == '__main__':
    with open('./../buffer-splaynet/json/input.json', 'r') as r:
        data = json.load(r)
    array = data['array']
    new_array = np.zeros((len(array), len(array)))
    G = nx.Graph()
    for x in range(len(array)):
        for y in array[x]:
            new_array[x][y] += 1
            array[y].remove(x)
    for x in range(len(new_array)):
        for y in range(len(new_array)):
            if new_array[x][y] > 0.0:
                G.add_edge(x, y, weight=new_array[x][y])


    # first compute the best partition
    partition = community_louvain.best_partition(G)
    partition = collections.OrderedDict(sorted(partition.items()))
    return_array = []
    for key in partition:
        return_array.append(partition[key])
    cuts = max(return_array)+1
    dict = {'array': return_array, 'cuts': cuts}
    with open('./../buffer-splaynet/json/output.json', 'w') as w:
        json.dump(dict, w)
    print(dict)
