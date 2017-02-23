import subprocess


def bash(command, stop_on_fail=True, forgive_error=[]):
    if isinstance(command, list):
        command = [str(c) for c in command]
        command = ' '.join(command)

    try:
        output = subprocess.check_output(command, shell=True).decode('utf-8')
    except subprocess.CalledProcessError as e:
        if stop_on_fail:
            if e.returncode in forgive_error:
                return None
            print('==Command failed== returncode: {}: {}'.format(e.returncode, command))
            raise e
        else:
            print('==Command failed== returncode: {}: {}'.format(e.returncode, command))
            return None

    return output.split()


def enum(*sequential, **named):
    enums = dict(zip(sequential, range(len(sequential))), **named)
    enums['list'] = sorted([(value, key) for key, value in enums.iteritems()], key=lambda x: x[0])
    return type('Enum', (), enums)


def listEnum(e, sep='\n'):
    res = ''
    for ind, name in e.list:
        res += '{}. {}{}'.format(ind, name, sep)
    return res
