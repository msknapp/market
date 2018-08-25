package knapp.advisor;

import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MixedAdvisor implements Advisor {
    private final Advisor core;
    private final List<List<String>> allInputs;

    MixedAdvisor(Advisor core, List<List<String>> allInputs) {
        this.core = core;
        this.allInputs = allInputs;
    }

    public static MixedAdvisorBuilder define() {
        return new MixedAdvisorBuilder();
    }


    @Override
    public void initialize() {
        core.initialize();
    }

    @Override
    public Advice getAdvice(List<String> ignoreThis) {
        // we just ignore the inputs.

        List<Advice> allAdvice = new ArrayList<>();
        for (List<String> inputs : allInputs) {
            Advice advice = core.getAdvice(inputs);
            allAdvice.add(advice);
        }
        if (allAdvice.size() == 1) {
            return allAdvice.get(0);
        }

        CombinedAdvice advice = new CombinedAdvice(allAdvice,core.getAllInputsTable());

        if (core instanceof AdvisorImpl) {
            AdvisorImpl advisor = (AdvisorImpl)core;
            Advice ad2 = advisor.getAdvice(advisor.getAllInputsTable(),advice.getModel());
            advice.setSimulationResults(ad2.getBestSimulationResults());
            if (ad2.getBestFunction() instanceof EvolvableFunction) {
                advice.setMixedFunction((EvolvableFunction) ad2.getBestFunction());
            }
        }

        return advice;
    }

    @Override
    public Map<String, Table> getAllInputs() {
        return null;
    }

    @Override
    public Table getAllInputsTable() {
        return core.getAllInputsTable();
    }


    public static class MixedAdvisorBuilder {
        private Advisor core;
        private List<List<String>> allInputs = new ArrayList<>();

        public MixedAdvisorBuilder() {

        }

        public MixedAdvisorBuilder core(Advisor core) {
            this.core = core;
            return this;
        }

        public MixedAdvisorBuilder addInputList(List<String> x) {
            this.allInputs.add(x);
            return this;
        }

        public MixedAdvisorBuilder addInputs(String ... x) {
            this.allInputs.add(Arrays.asList(x));
            return this;
        }

        public MixedAdvisor build() {
            return new MixedAdvisor(core, allInputs);
        }
    }
}
