import pymetis
import json

def partition(adj_list, cuts: int):
    partitions, mem = pymetis.part_graph(cuts, adjacency=adj_list)
    return [partitions, mem]


if __name__ == '__main__':
    with open('./../buffer-splaynet/json/input.json', 'r') as r:
        data = json.load(r)
    prop = data['array']
    maxComponentSize = data['maxComponentSize']
    prop_cuts = 2
    done = False
    while not done:
        done = True
        n_cuts, membership = partition(prop, prop_cuts)
        for k in range(prop_cuts):
            count = 0
            for j in range(len(membership)):
                if membership[j] == k:
                    count += 1
            if count > maxComponentSize:
                prop_cuts += 1
                done = False
                break
    person_dict = {'array': membership, 'cuts': prop_cuts}
    with open('./../buffer-splaynet/json/output.json', 'w') as w:
        json.dump(person_dict, w)
    print('done')